package org.meridor.perspective.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Operation {

    CloudType cloud();
    
    OperationType type(); //TODO: support multiple operation types for one class
    
}
