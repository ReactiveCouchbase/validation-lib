package org.reactivecouchbase.validation;

import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {

    private final transient ValidationFailure<?, ?> failure;

    public ValidationException(ValidationFailure<?, ?> failure) {
        super(failure.getFailures().stream().filter(i -> i != null).map(Object::toString).collect(Collectors.joining("\n")));
        this.failure = failure;
    }

    @SuppressWarnings("unchecked")
    public <T, E> ValidationFailure<T, E> getFailure() {
        return (ValidationFailure<T, E>) failure;
    }
}
