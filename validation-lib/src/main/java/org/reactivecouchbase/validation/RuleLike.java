package org.reactivecouchbase.validation;

public interface RuleLike<I, O> {
    Validation<O, ValidationError> validate(I in);
}
