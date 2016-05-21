package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.meridor.perspective.sql.impl.storage.TableFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public abstract class BaseTableFetcher<T> implements TableFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(TableFetcher.class);

    @Autowired
    private ObjectMapperAware objectMapperAware;

    protected abstract Class<T> getBeanClass();

    protected abstract Collection<T> getRawData();

    @Override
    public List<List<Object>> fetch(Set<String> ids, List<Column> columns) {
        return prepareData(ids, columns);
    }

    @Override
    public abstract String getTableName();

    private List<List<Object>> prepareData(Set<String> ids, List<Column> columns) {
        Class<T> beanClass = getBeanClass();
        ObjectMapper<T> objectMapper = objectMapperAware.get(beanClass);
        String tableName = getTableName();
        try {
            List<String> availableColumnNames = objectMapper.getAvailableColumnNames();
            columns.forEach(c -> {
                String columnName = c.getName();
                if (!availableColumnNames.contains(columnName)) {
                    throw new IllegalArgumentException(String.format("Table \"%s\" does not contain column \"%s\"", tableName, columnName));
                }
            });
            Collection<T> rawEntities = getRawData();
            return rawEntities.stream()
                    .filter(re -> {
                        String entityId = objectMapper.getId(re);
                        return ids.isEmpty() || ids.contains(entityId);
                    })
                    .map(re -> {
                        Map<String, Object> rowAsMap = objectMapper.map(re);
                        return columns.stream()
                                .map(c -> {
                                    Object columnValue = rowAsMap.get(c.getName());
                                    return (columnValue == null && c.getDefaultValue() != null) ?
                                            c.getDefaultValue() :
                                            columnValue;
                                })
                                .collect(Collectors.toList());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(String.format("Failed to fetch \"%s\" table contents", tableName), e);
            return Collections.emptyList();
        }
    }

}
