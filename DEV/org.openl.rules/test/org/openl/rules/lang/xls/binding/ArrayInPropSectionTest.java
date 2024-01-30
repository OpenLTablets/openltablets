package org.openl.rules.lang.xls.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

public class ArrayInPropSectionTest extends BaseOpenlBuilderHelper {

    private static final String SRC = "test/rules/TestArrayInPropSection.xls";

    public ArrayInPropSectionTest() {
        super(SRC);
    }

    @Test
    public void testLoadingArrayInPropertyTableSection() {
        final String tableName = "Rules DoubleValue driverRiskScoreOverloadTest(String driverRisk)";
        TableSyntaxNode resultTsn = findTable(tableName);

        if (resultTsn != null) {
            assertEquals(resultTsn.getTableProperties().getTableProperties().size(),
                4,
                "Check that number of properties defined in table is 4");
            assertEquals("tag1", resultTsn.getTableProperties().getTags()[0]);
            assertEquals("tag3", resultTsn.getTableProperties().getTags()[1]);
            assertEquals("tag4", resultTsn.getTableProperties().getTags()[2]);
        } else {
            fail();
        }
    }

}
