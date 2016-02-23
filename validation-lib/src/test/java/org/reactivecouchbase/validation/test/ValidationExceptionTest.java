package org.reactivecouchbase.validation.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.reactivecouchbase.validation.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationExceptionTest {

    private ValidationException validationException;

    @Before
    public void before() throws Exception {
        ValidationFailure<?, ?> failure = (ValidationFailure<?, ?>) Validation.failure(ValidationError.at(Paths.Root, "Error"));
        validationException = new ValidationException(failure);
    }

    @Test
    public void testGetFailure() throws Exception {
        assertThat(validationException.getFailure()).isNotNull();
        assertThat(validationException.getFailure().getFailures()).isNotNull();
        assertThat(validationException.getFailure().getFailures().size()).isNotNull();
        assertThat(validationException.getFailure().getFailures().size()).isEqualTo(1);
    }

} 
