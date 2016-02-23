package org.reactivecouchbase.validation.test;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.reactivecouchbase.validation.*;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleTest {

    private Rule<String, Integer> rule = new Rule<String, Integer>() {
        @Override
        public Validation<Integer, ValidationError> validate(String in) {
            try {
                return Validation.success(Integer.valueOf(in));
            } catch (Exception e) {
                return Validation.failure(ValidationError.of(e));
            }
        }
    };

    @Test
    public void testComposeForPathSub() throws Exception {
        for (List<ValidationError> errors : rule.compose(Paths.Root.field("test"), Rules.<Integer, Integer>fail()).validate("123").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/ test");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testComposeSub() throws Exception {
        for (List<ValidationError> errors : rule.compose(Rules.<Integer, Integer>fail()).validate("123").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testFlatMap() throws Exception {
        assertThat(rule.validate("123").flatMap(input -> Validation.success(input.toString())).get()).isEqualTo("123");
    }

    @Test
    public void testOrElse() throws Exception {
        assertThat(rule.validate("123qds").orElse(Validation.<Integer, ValidationError>success(456)).get()).isEqualTo(456);
    }

    @Test
    public void testCombine() throws Exception {
        assertThat(rule.combine(Rules.<String, Integer>fail()).validate("123").isFailure()).isTrue();
    }

    @Test
    public void testRepathPath() throws Exception {
        for (List<ValidationError> errors : rule.combine(Rules.<String, Integer>fail()).repath(Paths.Root.field("blah")).validate("123").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/ blah");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testRepathF() throws Exception {
        for (List<ValidationError> errors : rule.combine(Rules.<String, Integer>fail()).repath(input -> {
            assertThat(input.toString()).isEqualTo("/");
            return Paths.Root.field("blah");
        }).validate("123").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/ blah");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testFromRule() throws Exception {
        Rule<String, Integer> r = Rule.from(new RuleLike<String, Integer>() {
            @Override
            public Validation<Integer, ValidationError> validate(String in) {
                try {
                    return Validation.success(Integer.valueOf(in));
                } catch (Exception e) {
                    return Validation.failure(ValidationError.of(e));
                }
            }
        });
        assertThat(r.validate("123").get()).isEqualTo(123);
    }

    @Test
    public void testValidateWithForPathPredicate() throws Exception {
        Rule<String, String> r = Rule.validateWith(Paths.Root.field("blah"), new Predicate<String>() {
            @Override
            public boolean test(String input) {
                try {
                    Integer.valueOf(input);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        assertThat(r.validate("123").isSuccess()).isTrue();
        for (List<ValidationError> errors : r.validate("123dsq").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/ blah");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testValidateWithPredicate() throws Exception {
        Rule<String, String> r = Rule.validateWith(new Predicate<String>() {
            @Override
            public boolean test(String input) {
                try {
                    Integer.valueOf(input);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        assertThat(r.validate("123").isSuccess()).isTrue();
    }

    @Test
    public void testValidateWithForErrorMessagePredicate() throws Exception {
        Rule<String, String> r = Rule.validateWith("blah", input -> {
            try {
                Integer.valueOf(input);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        assertThat(r.validate("123").isSuccess()).isTrue();
        for (List<ValidationError> errors : r.validate("123dsq").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.message).isNotNull();
                assertThat(error.message).isEqualTo("blah");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }

    @Test
    public void testValidateWithForPathErrorMessagePredicate() throws Exception {
        Rule<String, String> r = Rule.validateWith(Paths.Root.field("blah"), "blah", new Predicate<String>() {
            @Override
            public boolean test(String input) {
                try {
                    Integer.valueOf(input);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
        assertThat(r.validate("123").isSuccess()).isTrue();
        for (List<ValidationError> errors : r.validate("123dsq").onFailure()) {
            for (ValidationError error : errors) {
                assertThat(error.message).isNotNull();
                assertThat(error.message).isEqualTo("blah");
                assertThat(error.path).isNotNull();
                assertThat(error.path.toString()).isEqualTo("/ blah");
            }
            return;
        }
        Assertions.fail("Should not be there");
    }
}
