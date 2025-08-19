package org.hmxlabs.techtest.server.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidatorImpl.class)
@NotNull( message = "Enum value cannot be null")
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidator {

    Class<? extends Enum<?>> enumClazz();

    String message() default "Enum value is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
