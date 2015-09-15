package org.meridor.perspective.shell.repository.query.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SupportedInstanceState {
    
    org.meridor.perspective.beans.InstanceState[] value() default {};
    
}
