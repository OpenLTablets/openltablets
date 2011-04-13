package org.openl.rules.validator.dt;

import junit.framework.Assert;

import org.junit.Test;
import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.dt.DecisionTable;
import org.openl.rules.dt.validator.DesionTableValidationResult;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.syntax.exception.SyntaxNodeException;

public class ValidateDTTest extends BaseOpenlBuilderHelper {

    private static String __src = "test/rules/validation/TestValidateDT.xls";

    public ValidateDTTest() {
        super(__src);
    }

    @Test
    public void testHello1() {
        String tableName = "Rules int tableAScore(String maritalStatus, String gender, int tyde, int dsr)";
        TableSyntaxNode resultTsn = findTable(tableName);
        Assert.assertNotNull(resultTsn);

        Object vv = resultTsn.getValidationResult();

        System.out.println("Hello1:" + vv);

        Assert.assertEquals(1, ((DesionTableValidationResult)vv).getOverlappings().length);
        Assert.assertEquals(0, ((DesionTableValidationResult)vv).getUncovered().length);
    }

    @Test
    public void testHello2() {
        String tableName = "SimpleRules String check2(String dd1, String dd2)";
        TableSyntaxNode resultTsn = findTable(tableName);
        Assert.assertNotNull(resultTsn);

        Object vv = resultTsn.getValidationResult();
        

        System.out.println("Hello2:" + vv);

        Assert.assertEquals(1, ((DesionTableValidationResult)vv).getOverlappings().length);
        Assert.assertEquals(1, ((DesionTableValidationResult)vv).getUncovered().length);
        
    }

    @Test
    public void testHello3() {
        String tableName = "Rules String check3(int dd1, String dd2)";
        TableSyntaxNode resultTsn = findTable(tableName);
        Assert.assertNotNull(resultTsn);

        Object vv = resultTsn.getValidationResult();

        System.out.println("Hello3:" + vv);

        SyntaxNodeException[] err = resultTsn.getErrors();

        if (err != null)
            for (int i = 0; i < err.length; i++) {
                System.out.println(err[i]);
            }
        Assert.assertEquals(1, ((DesionTableValidationResult)vv).getOverlappings().length);
        Assert.assertEquals(1, ((DesionTableValidationResult)vv).getUncovered().length);

    }

    
    @Test
    public void testHello4() {
        String tableName = "Rules String check4(int dd1, String dd2)";
        TableSyntaxNode resultTsn = findTable(tableName);
        Assert.assertNotNull(resultTsn);

        Object vv = resultTsn.getValidationResult();

        System.out.println("------- Hello4:" + vv);

        SyntaxNodeException[] err = resultTsn.getErrors();

        if (err != null)
            for (int i = 0; i < err.length; i++) {
                System.out.println(err[i]);
            }

        
        Assert.assertNull(vv);
    }
    
}
