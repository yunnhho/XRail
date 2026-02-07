package com.dev.XRail.common.validator;

import com.dev.XRail.common.annotation.Honeypot;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class HoneypotValidator implements ConstraintValidator<Honeypot, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 값이 존재하면 봇으로 간주 (False)
        return !StringUtils.hasText(value);
    }
}