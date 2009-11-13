package org.openl.rules.ui;

import org.openl.rules.indexer.IIndexElement;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.table.properties.ITableProperties;

public class CategorySorter extends ATableTreeSorter implements IProjectTypes {

    String getCategory(TableSyntaxNode tsn) {
        String category = null;
        ITableProperties tableProp = tsn.getTableProperties();
        if (tableProp != null && tableProp.getPropertyValue("category") != null) { 
             category = tableProp.getPropertyValueAsString("category");
        }

        if (category == null) {

            XlsSheetSourceCodeModule sheet = tsn.getXlsSheetSourceCodeModule();
            category = sheet.getSheetName();
        }
        return category;

    }

    @Override
    public String[] getDisplayValue(Object sorterObject, int i) {
        String category = getCategory((TableSyntaxNode) sorterObject);
        return new String[] { category, category, category };
    }

    @Override
    public String getName() {
        return "category";
    }

    @Override
    public String getType(Object sorterObject) {
        return "category";
    }

    @Override
    public String getUrl(Object sorterObject) {
        IIndexElement ie = (IIndexElement) sorterObject;
        return ie.getUri();
    }

    @Override
    public int getWeight(Object sorterObject) {
        return 0;
    }

    @Override
    public Object makeSorterObject(TableSyntaxNode tsn) {
        return tsn;
    }

}
