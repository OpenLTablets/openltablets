package org.openl.rules.ui;

import org.apache.commons.lang.StringUtils;
import org.openl.rules.lang.xls.ITableNodeTypes;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;
import org.openl.rules.table.properties.def.TablePropertyDefinitionUtils;
import org.openl.types.IOpenMethod;

public class TableSyntaxNodeUtils {

    private static final String DISPLAY_TABLE_PROPERTY_NAME = "display";
    private static final String NAME_TABLE_PROPERTY_NAME = "name";

    public static String[] getTableDisplayValue(TableSyntaxNode tableSyntaxNode) {
        
        return getTableDisplayValue(tableSyntaxNode, 0);
    }

    public static String[] getTableDisplayValue(TableSyntaxNode tableSyntaxNode, int i) {
        
        return getTableDisplayValue(tableSyntaxNode, i, null);
    }

    public static String[] getTableDisplayValue(TableSyntaxNode tableSyntaxNode, int i, OpenMethodGroupsDictionary dictionary) {
        
        ITableProperties tableProperties = tableSyntaxNode.getTableProperties();
        
        String display = null;
        String name = null;

        if (tableProperties != null) {

            name = tableProperties.getPropertyValueAsString(NAME_TABLE_PROPERTY_NAME);
            display = tableProperties.getPropertyValueAsString(DISPLAY_TABLE_PROPERTY_NAME);
        
            if (display == null) {
                display = name;
            }
        }

        if (name == null) {
            name = str2name(tableSyntaxNode.getTable().getGridTable().getCell(0, 0).getStringValue(), tableSyntaxNode.getType());
        }

        if (display == null) {
            display = str2display(tableSyntaxNode.getTable().getGridTable().getCell(0, 0).getStringValue(), tableSyntaxNode.getType());
        }

        String sfx = (i < 2 ? "" : "(" + i + ")");
        String dimensionInfo = StringUtils.EMPTY;

        if (dictionary != null && tableProperties != null && tableSyntaxNode.getMember() instanceof IOpenMethod
                && dictionary.contains((IOpenMethod) tableSyntaxNode.getMember())) {

            String[] dimensionalPropertyNames = TablePropertyDefinitionUtils.getDimensionalTableProperties();

            for (String dimensionalPropertyName : dimensionalPropertyNames) {
                String value = tableProperties.getPropertyValueAsString(dimensionalPropertyName);

                if (!StringUtils.isEmpty(value)) {
                    String propertyInfo = StringUtils.join(new Object[] { dimensionalPropertyName, "=", value });
                    dimensionInfo = StringUtils.join(new Object[] { dimensionInfo,
                            StringUtils.isEmpty(dimensionInfo) ? StringUtils.EMPTY : ", ", propertyInfo });
                }

            }
        }

        if (!StringUtils.isEmpty(dimensionInfo)) {
            sfx = StringUtils.join(new Object[] { sfx, StringUtils.isEmpty(sfx) ? StringUtils.EMPTY : " ", "[",
                    dimensionInfo, "]" });
        }

        return new String[] { name + sfx, display + sfx, display + sfx };
    }

    private static String str2display(String src, String type) {

        return src;
    }

    public static String str2name(String src, String type) {
        if (src == null) {
            src = "NO NAME";
        } else if (type.equals(ITableNodeTypes.XLS_DT) || type.equals(ITableNodeTypes.XLS_SPREADSHEET)
                || type.equals(ITableNodeTypes.XLS_TBASIC) || type.equals(ITableNodeTypes.XLS_COLUMN_MATCH)
                || type.equals(ITableNodeTypes.XLS_DATA) || type.equals(ITableNodeTypes.XLS_DATATYPE)
                || type.equals(ITableNodeTypes.XLS_METHOD) || type.equals(ITableNodeTypes.XLS_TEST_METHOD)
                || type.equals(ITableNodeTypes.XLS_RUN_METHOD)) {

            String[] tokens = StringUtils.split(src.replaceAll("\\(.*\\)", ""));
            src = tokens[tokens.length - 1].trim();
        }
        return src;
    }
}
