package org.meridor.perspective.rest.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Consume {

    String STORAGE_KEY = "storageKey";

    String storageKey() default "";
    
}
