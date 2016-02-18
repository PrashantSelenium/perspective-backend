package org.meridor.perspective.sql.impl.storage;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;

import java.util.List;
import java.util.stream.Collectors;

public interface DataFetcher {
    
    DataContainer fetch(TableName tableName, String tableAlias, List<Column> columns);
    
    default List<String> columnsToNames(List<Column> columns) {
        return columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }
    
}
