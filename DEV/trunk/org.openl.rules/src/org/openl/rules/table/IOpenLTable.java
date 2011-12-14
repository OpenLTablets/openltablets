package org.openl.rules.table;

import java.util.List;

import org.openl.message.OpenLMessage;
import org.openl.rules.table.properties.ITableProperties;

public interface IOpenLTable {

    IGridTable getGridTable();

    IGridTable getGridTable(String view);

    ITableProperties getProperties();

    String getType();

    List<OpenLMessage> getMessages();

    /**
     * @return Table name for user. (Firstly will be searched in table
     *         properties and then from table header)
     */
    String getName();
    String getTechnicalName();

    /**
     * 
     * @return true if table is executable at OpenL rules runtime. Also it indicates that tests can be created for this 
     * table.   
     */
    boolean isExecutable();
    
    /**
     * 
     * @return true if table supports operations over versions
     */
    boolean isVersionable();

    String getUri();

}
