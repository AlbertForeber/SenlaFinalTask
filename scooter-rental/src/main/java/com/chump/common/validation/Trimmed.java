package com.chump.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TrimmedValidator.class)
public @interface Trimmed {

    String message() default "must not contain trailing spaces or be blank";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
