package com.redcare.githubpopularity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class MinYearValidator implements ConstraintValidator<MinYear, LocalDate> {

    private int minYear;

    @Override
    public void initialize(MinYear annotation) {
        this.minYear = annotation.value();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.getYear() >= minYear;
    }
}
