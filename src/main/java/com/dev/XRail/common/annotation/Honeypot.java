package com.dev.XRail.common.annotation;

import com.dev.XRail.common.validator.HoneypotValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [Anti-Bot] Honeypot Field Check
 * 봇이 자동으로 채우는 숨겨진 필드를 감지하여 요청을 차단합니다.
 * 해당 필드는 반드시 비어있어야 합니다 (null or empty string).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HoneypotValidator.class)
public @interface Honeypot {
    String message() default "Bot detected.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}