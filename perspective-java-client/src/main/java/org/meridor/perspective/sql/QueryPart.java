package org.meridor.perspective.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QueryPart {
    
    String getSql();
    
    List<Parameter> getParameters();
    
    default Optional<QueryPart> getPreviousPart() {
        return Optional.empty();
    }

    default List<QueryPart> getParts() {
        List<QueryPart> queryParts = new ArrayList<>();
        if (getPreviousPart().isPresent()) {
            QueryPart prevQueryPart = getPreviousPart().get();
            queryParts.addAll(prevQueryPart.getParts());
        }
        queryParts.add(this);
        return queryParts;
    }

    default Query getQuery() {
        String sql = getParts().stream()
                .map(QueryPart::getSql)
                .collect(Collectors.joining());
        List<Parameter> parameters = getParts().stream()
                .flatMap(p -> p.getParameters().stream())
                .collect(Collectors.toList());
        Query query = new Query();
        query.setSql(sql);
        List<Parameter> parametersEntity = new ArrayList<>();
        parametersEntity.addAll(parameters);
        query.setParameters(parametersEntity);
        return query;
    }

}
