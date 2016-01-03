package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.BooleanRelation;
import org.springframework.stereotype.Component;

@Component
public class RelationChecker {
    
    public boolean checkDoubleRelation(double doubleValue, double number, BooleanRelation relation) {
        switch (relation) {
            default:
            case EQUAL: return doubleValue == number;
            case NOT_EQUAL: return doubleValue != number;
            case GREATER_THAN: return doubleValue > number;
            case LESS_THAN: return doubleValue < number;
            case GREATER_THAN_EQUAL: return doubleValue >= number;
            case LESS_THAN_EQUAL: return doubleValue <= number;
        }
    }
    
}
