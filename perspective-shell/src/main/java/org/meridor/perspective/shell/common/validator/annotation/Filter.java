package org.meridor.perspective.shell.common.validator.annotation;

import org.meridor.perspective.shell.common.validator.Field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Filter {
    
    Field value();
    
}
