package org.meridor.perspective.sql.impl;

import java.util.Optional;

public class LimitClause extends BaseQueryPart {

    private final int limit;
    private final int offset;
    private final QueryPart previousQueryPart;

    
    public LimitClause(QueryPart previousQueryPart, int limit) {
        this(previousQueryPart, limit, 0);
    }
    
    public LimitClause(QueryPart previousQueryPart, int limit, int offset) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit should be a positive number");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset should be a non-negative number");
        }
        this.previousQueryPart = previousQueryPart;
        this.limit = limit;
        this.offset = offset;
        addToSql(" limit");
        if (offset > 0) {
            addToSql(String.format(" %d, %d", offset, limit));
        } else {
            addToSql(String.format(" %d", limit));
        }
    }
    
    @Override
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }
}
