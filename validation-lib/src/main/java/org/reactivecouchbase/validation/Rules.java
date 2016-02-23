package org.reactivecouchbase.validation;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.reactivecouchbase.functional.Unit;
import org.reactivecouchbase.json.mapping.ReaderConstraints;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.reactivecouchbase.validation.Rule.validateWith;

public class Rules {

    private Rules() {
    }

    public static <I> Rule<I, I> combine(Rule<I, I>... rules) {
        return combine(Arrays.asList(rules));
    }

    public static <I> Rule<I, I> combine(Iterable<Rule<I, I>> rules) {
        Rule<I, I> last = Rules.<I>pass();
        for (Rule<I, I> rule : rules) {
            last = last.combine(rule);
        }
        return last;
    }

    public static <I> Rule<I, I> pass() {
        return new Rule<I, I>() {
            @Override
            public Validation<I, ValidationError> validate(I in) {
                return Validation.success(in);
            }
        };
    }

    public static <I, O> Rule<I, O> fail() {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                return Validation.failure(ValidationError.of("Always fail"));
            }
        };
    }

    public static <I, O> Rule<I, O> pass(final O o) {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                return Validation.success(o);
            }
        };
    }

    public static <I, O, C extends Collection<O>> Rule<Iterable<I>, C> collection(final Supplier<C> builder, final Rule<I, O> rule) {
        return new Rule<Iterable<I>, C>() {
            @Override
            public Validation<C, ValidationError> validate(Iterable<I> ins) {
                C outs = builder.get();
                List<ValidationError> errors = new ArrayList<>();
                for (I in : ins) {
                    Validation<O, ValidationError> errorValidation = rule.validate(in);
                    if (errorValidation.isSuccess()) {
                        outs.add(errorValidation.getSuccess());
                    }
                    if (errorValidation.isFailure()) {
                        errors.addAll(errorValidation.getFailures());
                    }
                }
                if (errors.isEmpty()) {
                    return Validation.success(outs);
                }
                return Validation.failure(errors);
            }
        };
    }

    public static <I, O> Rule<Iterable<I>, List<O>> list(final Rule<I, O> rule) {
        return collection(ArrayList::new, rule);
    }

    public static <I, O> Rule<Iterable<I>, Set<O>> set(final Rule<I, O> rule) {
        return collection(HashSet::new, rule);
    }

    public static <I, F, O> Rule<I, O> pathRule(final Function<I, F> extractor, final Rule<F, O> rule) {
        return pathRule(Paths.Root, extractor, rule);
    }

    public static <I, F, O> Rule<I, O> pathRule(final Paths.Path path, final Function<I, F> extractor, final Rule<F, O> rule) {
        return new Rule<I, O>() {
            @Override
            public Validation<O, ValidationError> validate(I in) {
                F f = extractor.apply(in);
                return Validation.repath(rule.validate(f), path);
            }
        };
    }

    public static Rule<String, String> email() {
        return pattern("The specified value is not an email address", ReaderConstraints.EMAIL_PATTERN);
    }

    public static Rule<String, String> url() {
        return pattern("The specified value is not an email address", ReaderConstraints.URL_PATTERN);
    }

    public static Rule<String, String> phone() {
        return pattern("The specified value is not an email address", ReaderConstraints.PHONE_PATTERN);
    }

    public static Rule<String, String> pattern(final String error, final String p) {
        return validateWith(error, new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input != null && input.matches(p);
            }
        });
    }

    public static Rule<String, String> pattern(final String p) {
        return validateWith("The specified value does not match pattern " + p,
                input -> input != null && input.matches(p));
    }

    public static Rule<Integer, Integer> minI(final Integer value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<Integer>() {
            @Override
            public boolean test(Integer input) {
                return input != null && input > value;
            }
        });
    }

    public static Rule<Integer, Integer> maxI(final Integer value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<Integer>() {
            @Override
            public boolean test(Integer input) {
                return input != null && input < value;
            }
        });
    }

    public static Rule<Long, Long> minL(final Long value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<Long>() {
            @Override
            public boolean test(Long input) {
                return input != null && input > value;
            }
        });
    }

    public static Rule<Long, Long> maxL(final Long value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<Long>() {
            @Override
            public boolean test(Long input) {
                return input != null && input < value;
            }
        });
    }

    public static Rule<Double, Double> minD(final Double value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<Double>() {
            @Override
            public boolean test(Double input) {
                return input != null && input > value;
            }
        });
    }

    public static Rule<Double, Double> maxD(final Double value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<Double>() {
            @Override
            public boolean test(Double input) {
                return input != null && input < value;
            }
        });
    }

    public static Rule<Short, Short> minS(final Short value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<Short>() {
            @Override
            public boolean test(Short input) {
                return input != null && input > value;
            }
        });
    }

    public static Rule<Short, Short> maxS(final Short value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<Short>() {
            @Override
            public boolean test(Short input) {
                return input != null && input < value;
            }
        });
    }

    public static Rule<Float, Float> minF(final Float value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<Float>() {
            @Override
            public boolean test(Float input) {
                return input != null && input > value;
            }
        });
    }

    public static Rule<Float, Float> maxF(final Float value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<Float>() {
            @Override
            public boolean test(Float input) {
                return input != null && input < value;
            }
        });
    }

    public static Rule<BigDecimal, BigDecimal> minBD(final BigDecimal value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<BigDecimal>() {
            @Override
            public boolean test(BigDecimal input) {
                return input != null && input.compareTo(value) > 0;
            }
        });
    }

    public static Rule<BigDecimal, BigDecimal> maxBD(final BigDecimal value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<BigDecimal>() {
            @Override
            public boolean test(BigDecimal input) {
                return input != null && input.compareTo(value) < 0;
            }
        });
    }

    public static Rule<BigInteger, BigInteger> minBI(final BigInteger value) {
        return validateWith("The specified value is smaller than " + value, new Predicate<BigInteger>() {
            @Override
            public boolean test(BigInteger input) {
                return input != null && input.compareTo(value) > 0;
            }
        });
    }

    public static Rule<BigInteger, BigInteger> maxBI(final BigInteger value) {
        return validateWith("The specified value is bigger than " + value, new Predicate<BigInteger>() {
            @Override
            public boolean test(BigInteger input) {
                return input != null && input.compareTo(value) < 0;
            }
        });
    }

    public static <I> Rule<I, I> ignore() {
        return new Rule<I, I>() {
            @Override
            public Validation<I, ValidationError> validate(I in) {
                return Validation.success(in);
            }
        };
    }

    public static <I> Rule<I, I> notNull() {
        return validateWith("The specified value is null", new Predicate<I>() {
            @Override
            public boolean test(I input) {
                return input != null;
            }
        });
    }

    public static <I> Rule<I, I> isNull() {
        return new Rule<I, I>() {
            @Override
            public Validation<I, ValidationError> validate(I in) {
                if (in == null) {
                    return Validation.success(in);
                }
                return Validation.failure(new ValidationError("The specified value is not null"));
            }
        };
    }

    public static <I> Rule<I, I> equalsTo(final I to) {
        return validateWith("The specified value is not equals to reference object (" + to.toString() + ")", new Predicate<I>() {
            @Override
            public boolean test(I input) {
                return input != null && input.equals(to);
            }
        });
    }

    public static Rule<String, String> notEmptyStr() {
        return validateWith("The specified value is an empty String", new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input != null && !input.isEmpty();
            }
        });
    }

    public static Rule<String, String> emptyStr() {
        return validateWith("The specified value is not an empty String", new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input != null && input.isEmpty();
            }
        });
    }

    public static <I extends Collection> Rule<I, I> notEmpty() {
        return validateWith("The specified value is an empty collection", new Predicate<I>() {
            @Override
            public boolean test(I input) {
                return input != null && !input.isEmpty();
            }
        });
    }

    public static <I extends Collection> Rule<I, I> isEmpty() {
        return validateWith("The specified value is not an empty collection", new Predicate<I>() {
            @Override
            public boolean test(I input) {
                return input != null && input.isEmpty();
            }
        });
    }

    public static Rule<String, String> minLength(final int size) {
        return validateWith(new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input != null && input.length() >= size;
            }
        });
    }

    public static Rule<String, String> maxLength(final int size) {
        return validateWith(new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input != null && input.length() <= size;
            }
        });
    }

    public static Rule<String, Date> date(final String pattern) {
        return new Rule<String, Date>() {
            SimpleDateFormat df = new SimpleDateFormat(pattern);

            @Override
            public Validation<Date, ValidationError> validate(String in) {
                if (in == null) {
                    return Validation.failure(new ValidationError("Input can't be null"));
                }
                try {
                    return Validation.success(df.parse(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }

    public static Rule<String, DateTime> dateTime(final String pattern) {
        return new Rule<String, DateTime>() {
            @Override
            public Validation<DateTime, ValidationError> validate(String in) {
                if (in == null) {
                    return Validation.failure(new ValidationError("Input can't be null"));
                }
                try {
                    return Validation.success(DateTime.parse(in, DateTimeFormat.forPattern(pattern)));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError(e));
                }
            }
        };
    }

    public static Rule<Integer, Integer> greaterThan(final int value) {
        return validateWith("The specified value is lesser than " + value, new Predicate<Integer>() {
            @Override
            public boolean test(Integer input) {
                return input > value;
            }
        });
    }

    public static Rule<Integer, Integer> lesserThan(final int value) {
        return validateWith("The specified value is greater than " + value, new Predicate<Integer>() {
            @Override
            public boolean test(Integer input) {
                return input < value;
            }
        });
    }

    public static Rule<String, String> mandatory() {
        return Rule.from(new RuleLike<String, String>() {
            @Override
            public Validation<String, ValidationError> validate(String in) {
                if (in == null) {
                    return Validation.failure(ValidationError.of("Validated value is null"));
                }
                if (in.trim().isEmpty()) {
                    return Validation.failure(ValidationError.of("Validated value is empty"));
                }
                return Validation.success(in);
            }
        });
    }

    public static Rule<String, Integer> isInteger() {
        return Rule.from(new RuleLike<String, Integer>() {
            @Override
            public Validation<Integer, ValidationError> validate(String in) {
                try {
                    return Validation.success(Integer.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError("Not an integer"));
                }
            }
        });
    }

    public static Rule<String, Boolean> isBoolean() {
        return Rule.from(new RuleLike<String, Boolean>() {
            @Override
            public Validation<Boolean, ValidationError> validate(String in) {
                try {
                    return Validation.success(Boolean.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError("Not an integer"));
                }
            }
        });
    }

    public static Rule<String, Long> isLong() {
        return Rule.from(new RuleLike<String, Long>() {
            @Override
            public Validation<Long, ValidationError> validate(String in) {
                try {
                    return Validation.success(Long.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError("Not an integer"));
                }
            }
        });
    }

    public static Rule<String, Double> isDouble() {
        return Rule.from(new RuleLike<String, Double>() {
            @Override
            public Validation<Double, ValidationError> validate(String in) {
                try {
                    return Validation.success(Double.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError("Not an integer"));
                }
            }
        });
    }

    public static Rule<String, Float> isFLoat() {
        return Rule.from(new RuleLike<String, Float>() {
            @Override
            public Validation<Float, ValidationError> validate(String in) {
                try {
                    return Validation.success(Float.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(new ValidationError("Not an integer"));
                }
            }
        });
    }
}
