package org.reactivecouchbase.validation.test;

import org.junit.Test;
import org.reactivecouchbase.validation.*;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationTest {

    public class Entity {
        public String name;
        public Integer age;
        public String email;

        public Entity() {
        }

        public Entity(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }

    @Test
    public void simpleValidationTest() {
        assertThat(
                Rules.email().validate("john.doe@gmail.com").isSuccess()
        ).isTrue();

        assertThat(
                Rules.url().validate("https://www.google.fr").isSuccess()
        ).isTrue();

        assertThat(
                Rules.url().validate("http:/www.google.fr").isFailure()
        ).isTrue();

        assertThat(
                Rules.phone().validate("0606060606").isSuccess()
        ).isTrue();
    }

    @Test @SuppressWarnings("unchecked")
    public void complexEntityTest() {
        Entity entity = new Entity("John Doe", 42, "john.doe@gmail.com");

        Rule<Entity, Entity> entityValidator =
            Rules.<Integer>notNull()
                .combine(Rules.minI(18))
                .combine(Rules.maxI(99))
                .repath("age")
                .innerValidation(new Function<Entity, Integer>() {
                    @Override
                    public Integer apply(Entity input) {
                        return input.age;
                    }
                }).combine(
            Rules.<String>notNull()
                .combine(Rules.equalsTo("John Doe"))
                .repath("name")
                .innerValidation(new Function<Entity, String>() {
                    @Override
                    public String apply(Entity input) {
                        return input.name;
                    }
                })).combine(
            Rules.<String>notNull()
                .combine(Rules.email())
                .repath("email")
                .innerValidation(new Function<Entity, String>() {
                    @Override
                    public String apply(Entity input) {
                        return input.email;
                    }
                }));

        assertThat(
                entityValidator.validate(entity).isSuccess()
        ).isTrue();

        assertThat(
                entityValidator.validate(new Entity()).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 100, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 12, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("Jane Doe", 42, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 42, "john.doe@com")).isSuccess()
        ).isFalse();

        List<ValidationError> errors = entityValidator.validate(new Entity("Jane Doe", 100, "john.doe@com")).onFailure().get();
        assertThat(errors.size()).isEqualTo(3);
        System.out.println("--------------------------");
        errors.forEach(System.out::println);
    }

    @Test @SuppressWarnings("unchecked")
    public void complexEntityTest3() {

        Entity entity = new Entity("John Doe", 42, "john.doe@gmail.com");

        Rule<Entity, Entity> entityValidator = new Rule<Entity, Entity>() {
            @Override
            public Validation<Entity, ValidationError> validate(Entity in) {
                return Validation.of(in).withValidations(
                        Rules.<Entity>notNull().validate(in),
                        Rules.<String>notNull().combine(Rules.equalsTo("John Doe")).repath(Paths.Root.field("name")).validate(in.name),
                        Rules.<Integer>notNull().combine(Rules.maxI(99)).combine(Rules.minI(18)).repath(Paths.Root.field("age")).validate(in.age),
                        Rules.<String>notNull().combine(Rules.email()).repath(Paths.Root.field("email")).validate(in.email)
                );
            }
        };

        assertThat(
                entityValidator.validate(entity).isSuccess()
        ).isTrue();

        assertThat(
                entityValidator.validate(new Entity()).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 100, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 12, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("Jane Doe", 42, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 42, "john.doe@com")).isSuccess()
        ).isFalse();

        List<ValidationError> errors = entityValidator.validate(new Entity("Jane Doe", 100, "john.doe@com")).onFailure().get();
        assertThat(errors.size()).isEqualTo(3);
        System.out.println("--------------------------");
        errors.forEach(System.out::println);
    }

    @Test
    public void complexEntityTest4() {

        Entity entity = new Entity("John Doe", 42, "john.doe@gmail.com");

        Rule<Entity, Entity> entityValidator = new Rule<Entity, Entity>() {
            @Override
            public Validation<Entity, ValidationError> validate(Entity in) {
                try {
                    Rules.<Entity>notNull().validate(in).orThrow();
                    Rules.<String>notNull().combine(Rules.equalsTo("John Doe")).repath(Paths.Root.field("name")).validate(in.name).orThrow();
                    Rules.<Integer>notNull().combine(Rules.maxI(99)).combine(Rules.minI(18)).repath(Paths.Root.field("age")).validate(in.age).orThrow();
                    Rules.<String>notNull().combine(Rules.email()).repath(Paths.Root.field("email")).validate(in.email).orThrow();
                    return Validation.success(in);
                } catch (ValidationException e) {
                    return Validation.failure(e.<Entity, ValidationError>getFailure().getFailures());
                }
            }
        };

        assertThat(
                entityValidator.validate(entity).isSuccess()
        ).isTrue();

        assertThat(
                entityValidator.validate(new Entity()).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 100, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 12, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("Jane Doe", 42, "john.doe@gmail.com")).isSuccess()
        ).isFalse();

        assertThat(
                entityValidator.validate(new Entity("John Doe", 42, "john.doe@com")).isSuccess()
        ).isFalse();

        List<ValidationError> errors = entityValidator.validate(new Entity("Jane Doe", 100, "john.doe@com")).onFailure().get();
        assertThat(errors.size()).isEqualTo(1);
        System.out.println("--------------------------");
        errors.forEach(System.out::println);
    }
}
