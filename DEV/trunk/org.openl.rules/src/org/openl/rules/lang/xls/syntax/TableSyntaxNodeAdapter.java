package org.openl.rules.lang.xls.syntax;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openl.message.OpenLMessage;
import org.openl.message.OpenLMessagesUtils;
import org.openl.rules.lang.xls.XlsNodeTypes;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.inherit.PropertiesChecker;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IOpenMember;

public class TableSyntaxNodeAdapter implements IOpenLTable {

    private TableSyntaxNode tsn;

    public TableSyntaxNodeAdapter(TableSyntaxNode tsn) {
        if (tsn == null) {
            throw new IllegalArgumentException("TableSyntaxNode is null");
        }
        this.tsn = tsn;
    }

    public IGridTable getGridTable() {
        return tsn.getGridTable();
    }

    public IGridTable getGridTable(String view) {
        if (view != null) {
            ILogicalTable gtx = tsn.getTable(view);
            if (gtx != null) {
                return gtx.getSource();
            }
        }
        return getGridTable();
    }

    public ITableProperties getProperties() {
        return tsn.getTableProperties();
    }

    public String getType() {
        return tsn.getType();
    }

    public List<OpenLMessage> getMessages() {
        SyntaxNodeException[] errors = tsn.getErrors();
        return OpenLMessagesUtils.newMessages(errors);
    }

    public String getTechnicalName() {
        IOpenMember member = tsn.getMember();
        if (member != null) {
            return member.getName();
        } else if (tsn.getType().equals("xls.other")) {
            return tsn.getGridTable().getCell(0, 0).getObjectValue().toString();
        }
        
        return StringUtils.EMPTY;
    }

    public String getName() {
        ITableProperties properties = getProperties();
        if (properties != null) {
            String name = properties.getName();
            if (StringUtils.isNotBlank(name)) {
                String version = properties.getVersion();
                if (StringUtils.isNotBlank(version)) { 
                    return String.format("%s: %s", name, version);
                }
                return name;
            }
        }
        return getTechnicalName();
    }

    public boolean isExecutable() {
        return tsn.isExecutableNode();
    }

    public String getUri() {
        return tsn.getUri();
    }

    public boolean isVersionable() {
        return PropertiesChecker.isPropertySuitableForTableType("version", tsn.getType());        
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(getUri()).toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TableSyntaxNodeAdapter table = (TableSyntaxNodeAdapter) obj;

        return new EqualsBuilder().append(getUri(), table.getUri()).isEquals();
    }

    public boolean isCanContainProperties() {
        String tableType = getType();
        return tableType != null
                && !tableType.equals(XlsNodeTypes.XLS_OTHER.toString())
                && !tableType.equals(XlsNodeTypes.XLS_ENVIRONMENT.toString())
                && !tableType.equals(XlsNodeTypes.XLS_PROPERTIES.toString());
    }

}
