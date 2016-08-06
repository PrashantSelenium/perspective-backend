package org.meridor.perspective.sql.impl.index.impl;

import java.io.Serializable;
import java.util.*;

public class IndexSignature implements Serializable {
    
    private final Map<String, Set<String>> desiredColumns;

    public IndexSignature(String tableName, Set<String> columnNames) {
        this.desiredColumns = new TreeMap<String, Set<String>>(){
            {
                put(tableName, new TreeSet<>(columnNames));
            }
        };
    }

    public Map<String, Set<String>> getDesiredColumns() {
        return new HashMap<>(desiredColumns);
    }
    
    @Override
    public int hashCode() {
        return desiredColumns.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IndexSignature && obj.hashCode() == hashCode();
    }

    @Override
    public String toString() {
        return String.format("IndexSignature{%s}", desiredColumns);
    }
}
