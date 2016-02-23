package org.reactivecouchbase.validation;

import org.reactivecouchbase.functional.Option;

import java.util.List;

public class ValidationSuccess<T, E> extends Validation<T, E> {

    private final T value;

    ValidationSuccess(T value) {
        this.value = value;
    }

    @Override
    public T getSuccess() {
        return value;
    }

    @Override
    public List<E> getFailures() {
        throw new RuntimeException("Not a ValidationFailure !!!");
    }

    @Override
    public Boolean isFailure() {
        return false;
    }

    @Override
    public Boolean isSuccess() {
        return true;
    }

    @Override
    public Option<ValidationFailure<T, E>> asFailure() {
        return Option.none();
    }

    @Override
    public Option<ValidationSuccess<T, E>> asSuccess() {
        return Option.some(this);
    }
}
