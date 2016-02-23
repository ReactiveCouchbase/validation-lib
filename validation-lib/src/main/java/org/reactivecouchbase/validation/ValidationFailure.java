package org.reactivecouchbase.validation;

import org.reactivecouchbase.functional.Option;

import java.util.List;

public class ValidationFailure<T, E> extends Validation<T, E> {

    private final List<E> errors;

    ValidationFailure(List<E> errors) {
        this.errors = errors;
    }

    @Override
    public T getSuccess() {
        throw new RuntimeException("Not a ValidationSuccess !!!");
    }

    @Override
    public List<E> getFailures() {
        return errors;
    }

    @Override
    public Boolean isFailure() {
        return true;
    }

    @Override
    public Boolean isSuccess() {
        return false;
    }

    @Override
    public Option<ValidationFailure<T, E>> asFailure() {
        return Option.some(this);
    }

    @Override
    public Option<ValidationSuccess<T, E>> asSuccess() {
        return Option.none();
    }
}
