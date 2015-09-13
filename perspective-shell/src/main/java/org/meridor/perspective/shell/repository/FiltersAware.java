package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.repository.query.validator.Field;

import java.util.Map;
import java.util.Set;

public interface FiltersAware {
    
    boolean hasFilter(Field field);

    void setFilter(Field field, Set<String> value);

    void unsetFilter(Field field);
    
    Set<String> getFilter(Field field);
    
    Map<String, String> getFilters();
    
}
