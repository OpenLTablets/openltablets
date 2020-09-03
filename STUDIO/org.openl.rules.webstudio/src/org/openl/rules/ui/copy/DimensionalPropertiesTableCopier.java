package org.openl.rules.ui.copy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openl.rules.table.IOpenLTable;
import org.openl.rules.table.properties.def.DefaultPropertyDefinitions;
import org.openl.rules.table.properties.def.TablePropertyDefinition;
import org.openl.rules.table.properties.inherit.InheritanceLevel;
import org.openl.rules.table.properties.inherit.PropertiesChecker;
import org.openl.rules.tableeditor.renderkit.TableProperty;
import org.openl.rules.webstudio.WebStudioFormats;

/**
 * @author Andrei Astrouski
 */
public class DimensionalPropertiesTableCopier extends TableCopier {

    public DimensionalPropertiesTableCopier(IOpenLTable table) {
        super(table);
        checkPropertiesExistance();
    }

    private void checkPropertiesExistance() {
        TablePropertyDefinition[] propDefinitions = DefaultPropertyDefinitions.getDefaultDefinitions();
        for (TablePropertyDefinition propDefinition : propDefinitions) {
            if (propDefinition.isDimensional() && getProperty(propDefinition.getName()) == null) {
                TableProperty property = new TableProperty(propDefinition, WebStudioFormats.getInstance());
                getPropertiesManager().addProperty(property);
            }
        }
    }

    @Override
    public List<TableProperty> getPropertiesToDisplay() {
        IOpenLTable table = getTable();
        if (table == null) {
            // reset() was invoked for the wizard.
            return Collections.emptyList();
        }

        List<TableProperty> properties = new ArrayList<>();

        for (TableProperty property : getPropertiesManager().getProperties()) {
            if (property.isDimensional() && PropertiesChecker.isPropertySuitableForTableType(property.getName(),
                table.getType()) && PropertiesChecker.isPropertySuitableForLevel(InheritanceLevel.TABLE,
                    property.getName())) {
                properties.add(property);
            }
        }

        return properties;
    }

}
