package org.meridor.perspective.sql.impl.expression;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class IsNullExpression implements BooleanExpression {
    
    private final Object value;

    public IsNullExpression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Set<String> getTableAliases() {
        if (value instanceof BooleanExpression) {
            return ((BooleanExpression) value).getTableAliases();
        }
        return Collections.emptySet();
    }

    @Override
    public Optional<BooleanExpression> getRestOfExpression() {
        return Optional.of(this);
    }
}
