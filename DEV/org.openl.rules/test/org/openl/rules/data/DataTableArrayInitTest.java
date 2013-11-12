package org.openl.rules.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.openl.engine.OpenLSystemProperties;
import org.openl.rules.BaseOpenlBuilderHelper;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

/*
 * @author PTarasevich
 */

public class DataTableArrayInitTest extends BaseOpenlBuilderHelper{
    private static final String FILE_NAME = "test/rules/testmethod/TestDataAccessFieldTest.xlsx";

    public DataTableArrayInitTest() {
        super(FILE_NAME);        
    }

    @Before
    public void before() {
        System.setProperty(OpenLSystemProperties.CUSTOM_SPREADSHEET_TYPE_PROPERTY, "true");
    }

    @Test
    public void testTypeWithArrayColumns() {
        String tableName = "Data TestHelperDataBean_v10 testArray ";
        TableSyntaxNode resultTsn = findTable(tableName);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();

            assertNotNull(member);

            Object[] typeWitharray = (Object[]) member.getTable().getDataArray();

            assertTrue(typeWitharray.length == 15);
            try {
                assertTrue(getAddressArry(typeWitharray[3]).length == 3);
                assertTrue(getAddressArry(typeWitharray[12])[1] == null);
                assertTrue(getZip(getAddressArry(typeWitharray[12])[0]) == 37);
                assertTrue(getZip(getAddressArry(typeWitharray[12])[2]) == 51);
            } catch (Exception e) {
               e.printStackTrace();
               fail();
            }
        } else {
            fail();
        }
    }

    @Test
    public void testTypeWithArray2Columns() {
        String tableName = "Data TestHelperDataBean_v10 testArray2";
        TableSyntaxNode resultTsn = findTable(tableName);
        if (resultTsn != null) {
            DataOpenField member = (DataOpenField)resultTsn.getMember();

            assertNotNull(member);

            Object[] typeWitharray = (Object[]) member.getTable().getDataArray();

            assertTrue(typeWitharray.length == 15);
            try {
                assertTrue(getVehicles(getP(typeWitharray[3])).length == 3);
                assertTrue(getModel(getVehicles(getP(typeWitharray[12]))[2]).equals("37"));
                assertTrue(getModel(getVehicles(getP(typeWitharray[12]))[1]).equals("51"));
                assertTrue(getVehicles(getP(typeWitharray[12]))[0] == null);
            } catch (Exception e) {
               e.printStackTrace();
               fail();
            }
        } else {
            fail();
        }
    }

    private Object getP(Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField("p");
        field.setAccessible(true);
        Object res = field.get(obj);
        field.setAccessible(false);
        return res;
    }

    private Object[] getVehicles(Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField("vehicles");
        field.setAccessible(true);
        Object[] res = (Object[]) field.get(obj);
        field.setAccessible(false);
        return res;
    }

    private String getModel(Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField("model");
        field.setAccessible(true);
        String res = (String) field.get(obj);
        field.setAccessible(false);
        return res;
    }

    private Object[] getAddressArry(Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField("addressArry");
        field.setAccessible(true);
        Object[] res = (Object[]) field.get(obj);
        field.setAccessible(false);
        return res;
    }

    private int getZip(Object obj) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField("zip");
        field.setAccessible(true);
        int res = Integer.parseInt(field.get(obj).toString());
        field.setAccessible(false);
        return res;
    }
}
