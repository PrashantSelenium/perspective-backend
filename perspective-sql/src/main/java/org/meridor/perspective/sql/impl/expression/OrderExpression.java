package org.meridor.perspective.sql.impl.expression;

public class OrderExpression {
    
    private final Object expression;
    private final OrderDirection orderDirection;

    public OrderExpression(Object expression) {
        this(expression, OrderDirection.ASC);
    }
    
    public OrderExpression(Object expression, OrderDirection orderDirection) {
        this.expression = expression;
        this.orderDirection = orderDirection;
    }

    public Object getExpression() {
        return expression;
    }

    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    @Override
    public String toString() {
        return "OrderExpression{" +
                "expression=" + expression +
                ", orderDirection=" + orderDirection +
                '}';
    }
}
