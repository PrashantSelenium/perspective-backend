package org.meridor.perspective.shell.validator.annotation;

import org.meridor.perspective.config.CloudType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SupportedCloud {
    
    CloudType[] value() default {};
    
}
