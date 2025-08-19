package org.hmxlabs.techtest.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.lang.SuppressWarnings;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    List<String> valueList = null;

    public void initialize(EnumValidator annotation) {
        valueList = new ArrayList<String>();
        Class<? extends Enum<?>> enumClass = annotation.enumClazz();

        @SuppressWarnings("rawtypes")
        Enum[] enumValueArray = enumClass.getEnumConstants();

        for (@SuppressWarnings("rawtypes") Enum enumValue : enumValueArray) {
            valueList.add(enumValue.toString().toUpperCase());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return valueList.contains(value.toUpperCase());
    }

}
