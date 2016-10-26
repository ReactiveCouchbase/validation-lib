package org.reactivecouchbase.validation;

import org.reactivecouchbase.functional.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Rule<I, O> implements RuleLike<I, O> {

    public O validateAndGet(I in) {
        return this.validate(in).get();
    }

    public Option<O> validateAndGetOpt(I in) {
        return this.validate(in).asOption();
    }

    public <P> Rule<I, P> compose(final Paths.Path path, final Rule<O, P> sub) {
        final Rule<I, O> that = this;
        final Rule<I, P> rule = that.flatMap(o -> new Rule<I, P>() {
            @Override
            public Validation<P, ValidationError> validate(I in) {
                return sub.validate(o);
            }
        });
        return new Rule<I, P>() {
            @Override
            public Validation<P, ValidationError> validate(I in) {
                Validation<P, ValidationError> validation = rule.validate(in);
                if (validation.isFailure()) {
                    List<ValidationError> errs = validation.getFailures()
                            .stream()
                            .map(input -> new ValidationError(input.path.compose(path), input.message))
                            .collect(Collectors.toList());
                    return Validation.failure(errs);
                }
                return validation;
            }
        };
    }

    public <P> Rule<I, P> compose(Rule<O, P> sub) {
        return compose(Paths.Root, sub);
    }

    public <B> Rule<I, B> flatMap(final Function<O, Rule<I, B>> f) {
        final Rule<I, O> self = this;
        return new Rule<I, B>() {
            @Override
            public Validation<B, ValidationError> validate(final I in) {
                return self.validate(in).map(f).foldWrapped(
                        Validation::failure,
                        input -> input.validate(in)
                );
            }
        };
    }

    public Rule<I, O> orElse(final RuleLike<I, O> other) {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                return this.validate(in).orElse(other.validate(in));
            }
        };
    }

    public Rule<I, O> combine(final Rule<I, O> other) {
        final Rule<I, O> self = this;
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                Validation<O, ValidationError> validation1 = self.validate(in);
                Validation<O, ValidationError> validation2 = other.validate(in);
                if (validation1.isSuccess() && validation2.isSuccess()) {
                    return validation2;
                } else if (validation1.isFailure() && validation2.isSuccess()) {
                    return Validation.failure(validation1.getFailures());
                } else if (validation1.isSuccess() && validation2.isFailure()) {
                    return Validation.failure(validation2.getFailures());
                } else {
                    List<ValidationError> errors = new ArrayList<>();
                    errors.addAll(validation1.getFailures());
                    errors.addAll(validation2.getFailures());
                    return Validation.failure(errors);
                }
            }
        };
    }

    /**
     * Allow to transform an atomic rule into a rule that can validate a complex object by validating a sub part of this object.
     * ie. validate something by validating a field/subpart/whatever inside it.
     */
    public <E> Rule<E, O> within(final Function<E, I> extractor) {
        final Rule<I, O> that = this;
        return Rule.from(new RuleLike<E, O>() {
            @Override
            public Validation<O, ValidationError> validate(E in) {
                return that.validate(extractor.apply(in));
            }
        });
    }

    /**
     * Allow to transform an atomic rule into a rule that can validate a complex object by validating a sub part of this object.
     * ie. validate something by validating a field/subpart/whatever inside it. Keep the object as output though
     */
    public <E> Rule<E, E> innerValidation(final Function<E, I> extractor) {
        final Rule<I, O> that = this;
        return Rule.from(new RuleLike<E, E>() {
            @Override
            public Validation<E, ValidationError> validate(E in) {
                Validation<O, ValidationError> validation = that.validate(extractor.apply(in));
                if (validation.isFailure()) {
                    return Validation.failure(validation.getFailures());
                }
                return Validation.success(in);
            }
        });
    }

    public Rule<I, O> rewriteErrorMessages(final String message) {
        return rewriteErrorMessages(input -> message);
    }

    public Rule<I, O> rewriteErrorMessages(final Function<String, String> rewrite) {
        return rewriteErrors(input -> ValidationError.at(input.path, rewrite.apply(input.message)));
    }

    public Rule<I, O> rewriteErrors(final Function<ValidationError, ValidationError> rewrite) {
        final Rule<I, O> that = this;
        return Rule.from(new RuleLike<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                Validation<O, ValidationError> validation = that.validate(in);
                if (validation.isFailure()) {
                    List<ValidationError> newErrors = validation.getFailures()
                        .stream()
                        .map(rewrite::apply)
                        .collect(Collectors.toList());
                    return Validation.failure(newErrors);
                }
                return validation;
            }
        });
    }

    public Rule<I, O> repath(final String path) {
        return repath(input -> Paths.parse(path));
    }

    public Rule<I, O> repath(final Paths.Path path) {
        return repath(input -> path);
    }

    public Rule<I, O> repath(final Function<Paths.Path, Paths.Path> f) {
        return rewriteErrors(input -> ValidationError.at(f.apply(input.path), input.message));
    }

    public static <I> Rule<I, I> from(final Function<I, RuleLike<I, ?>> rule) {
        return new Rule<I, I>() {
            @Override
            public Validation<I, ValidationError> validate(I in) {
                try {
                    Validation<?, ValidationError> validation = rule.apply(in).validate(in);
                    if (validation.isFailure()) {
                        return Validation.failure(validation.getFailures());
                    } else {
                        return Validation.success(in);
                    }
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }

    public static <I, O> Rule<I, O> from(final RuleLike<I, O> rule) {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                try {
                    return rule.validate(in);
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }

    public static <I, O> Rule<I, O> of(final Function<I, Validation<O, ValidationError>> rule) {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                try {
                    return rule.apply(in);
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }

    public static <I> Rule<I, I> validateWith(final Paths.Path path, final Predicate<I> predicate) {
        return validateWith(path, "Input does not match predicate", predicate);
    }


    public static <I> Rule<I, I> validateWith(final Predicate<I> predicate) {
        return validateWith("Input does not match predicate", predicate);
    }

    public static <I> Rule<I, I> validateWith(final String errorMessage, final Predicate<I> predicate) {
        return validateWith(Paths.Root, errorMessage, predicate);
    }

    public static <I> Rule<I, I> validateWith(final Paths.Path path, final String errorMessage, final Predicate<I> predicate) {
        return new Rule<I, I>() {
            @Override
            public Validation<I, ValidationError> validate(I in) {
                try {
                    if (in == null) {
                        return Validation.failure(ValidationError.at(path, "Input can't be null"));
                    }
                    if (predicate.test(in)) {
                        return Validation.success(in);
                    }
                    return Validation.failure(new ValidationError(path, errorMessage));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }
}
