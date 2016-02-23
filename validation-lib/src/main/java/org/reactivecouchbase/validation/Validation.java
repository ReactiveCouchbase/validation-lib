package org.reactivecouchbase.validation;

import org.reactivecouchbase.functional.Action;
import org.reactivecouchbase.functional.Option;
import org.reactivecouchbase.functional.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Validation<T, E> {

    public abstract T getSuccess();

    public abstract List<E> getFailures();

    public abstract Boolean isFailure();

    public abstract Boolean isSuccess();

    public static <T, E> Validation<T, E> success(T value) {
        return new ValidationSuccess<T, E>(value);
    }

    public static <T, E> Validation<T, E> failure(List<E> errors) {
        return new ValidationFailure<T, E>(errors);
    }

    @SuppressWarnings("unchecked")
    public static <T, E> Validation<T, E> failure(E error) {
        List<E> errors = new ArrayList<>(Collections.singletonList(error));
        return failure(errors);
    }

    public abstract Option<ValidationFailure<T, E>> asFailure();
    public abstract Option<ValidationSuccess<T, E>> asSuccess();

    public ValidationFailure<T, E> toFailure() {
        return asFailure().get();
    }

    public ValidationSuccess<T, E> toSuccess() {
        return asSuccess().get();
    }

    public Option<List<E>> onFailure() {
        if (isFailure()) {
            return Option.some(getFailures());
        }
        return Option.none();
    }

    public Option<T> onSuccess() {
        if (isFailure()) {
            return Option.none();
        }
        return Option.some(getSuccess());
    }

    public Option<T> asOption() {
        return onSuccess();
    }

    public Validation<T, E> orThrow() {
        if (isFailure()) {
            throw new ValidationException((ValidationFailure<T, E>) this);
        }
        return this;
    }

    public T get() {
        return asOption().get();
    }

    public T getOrElse(T t) {
        return asOption().getOrElse(t);
    }

    public Validation<T, E> orElse(Validation<T, E> t) {
        if (isSuccess()) {
            return this;
        }
        return t;
    }

    public <X> Validation<X, E> map(Function<T, X> f) {
        if (isSuccess()) {
            return success(f.apply(getSuccess()));
        }
        return failure(getFailures());
    }

    public <X> Validation<X, E> flatMap(Function<T, Validation<X, E>> f) {
        if (isSuccess()) {
            return f.apply(getSuccess());
        }
        return failure(getFailures());
    }

    public <X> X fold(Function<List<E>, X> invalid, Function<T, X> valid) {
        if (isSuccess()) {
            return valid.apply(getSuccess());
        }
        return invalid.apply(getFailures());
    }

    public <X> Validation<X, E> collect(E otherwise, Function<T, Option<X>> f) {
        if (isSuccess()) {
            Option<X> opt = f.apply(getSuccess());
            if (opt == null || opt.isEmpty()) {
                return failure(otherwise);
            }
            return success(opt.get());
        }
        return failure(getFailures());
    }

    public Validation<T, E> filterNot(final Predicate<T> predicate) {
        return filterNot(p -> !predicate.test(p));
    }

    public Validation<T, E> filter(Predicate<T> predicate) {
        if (isSuccess()) {
            T value = get();
            if (predicate.test(value)) {
                return Validation.success(value);
            }
            return Validation.failure(getFailures());
        }
        return Validation.failure(getFailures());
    }

    public void forEach(Function<T, Unit> function) {
        if (isSuccess()) {
            function.apply(get());
        }
    }

    public void forEach(Action<T> function) {
        if (isSuccess()) {
            function.apply(get());
        }
    }

    public Validation<T, E> recover(Function<ValidationFailure<T, E>, Option<T>> function) {
        if (isFailure()) {
            ValidationFailure<T, E> failure = (ValidationFailure<T, E>) this;
            Option<T> option = function.apply(failure);
            if (option.isDefined()) {
                return Validation.success(option.get());
            } else {
                return this;
            }
        }
        return Validation.success(get());
    }

    public Validation<T, E> recoverWith(Function<ValidationFailure<T, E>, T> manager) {
        if (isFailure()) {
            ValidationFailure<T, E> failure = (ValidationFailure<T, E>) this;
            return Validation.success(manager.apply(failure));
        }
        return Validation.success(get());
    }

    static <I> Validation<I, ValidationError> repath(Validation<I, ValidationError> validation, final Paths.Path path) {
        if (validation.isSuccess()) {
            return validation;
        }
        return Validation.failure(validation.getFailures().stream().map(input -> new ValidationError(path, input.message)).collect(Collectors.toList()));
    }

    private static <T, E> Validation<T, E> populateErrs(Validation<T, E> finalValidation, Validation<?, E>... validations) {
        List<E> failures = new ArrayList<>();
        for (Validation<?, E> validation : validations) {
            for (List<E> failureList : validation.onFailure()) {
                failures.addAll(failureList);
            }
        }
        if (failures.isEmpty() && finalValidation.isSuccess()) {
            return Validation.success(finalValidation.get());
        }
        return Validation.failure(failures);
    }

    public static <T> ContextualValidation<T, ValidationError> of(T input) {
        return new ContextualValidation<>(input);
    }

    public static <T, E> ContextualValidation<T, E> globalFor(T input) {
        return new ContextualValidation<T, E>(input);
    }

    public static <T, E> ContextualValidation<T, E> globalFor(T input, Class<E> clazz) {
        return new ContextualValidation<>(input);
    }

    public static class ContextualValidation<T, E> {

        private final T input;

        public ContextualValidation(T input) {
            this.input = input;
        }

        public Validation<T, E> withValidations(Validation<?, E>... subValidations) {
            List<E> failures = new ArrayList<>();
            for (Validation<?, E> validation : subValidations) {
                for (List<E> failureList : validation.onFailure()) {
                    failures.addAll(failureList);
                }
            }
            if (failures.isEmpty()) {
                return Validation.success(input);
            }
            return Validation.failure(failures);
        }
    }
}
