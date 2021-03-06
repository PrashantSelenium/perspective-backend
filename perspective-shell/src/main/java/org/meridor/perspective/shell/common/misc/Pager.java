package org.meridor.perspective.shell.common.misc;

import java.util.List;

public interface Pager {
    
    void page(String[] columns, List<String[]> rows);
    
    void page(List<String> pages);
    
    int getPageSize();

    boolean isExternalPaging();
    
}
