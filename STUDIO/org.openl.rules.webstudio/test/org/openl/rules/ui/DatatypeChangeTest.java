package org.openl.rules.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tika.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openl.config.ConfigurationManager;
import org.openl.rules.lang.xls.syntax.WorkbookSyntaxNode;
import org.openl.rules.project.instantiation.ReloadType;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.project.resolving.RulesProjectResolver;

@RunWith(Parameterized.class)
public class DatatypeChangeTest {
    private static final String SHEET_NAME = "Test";
    private static final String EXPENSE_MODULE_FILE_NAME = "ExpenseModule.xls";
    private static final String MAIN_MODULE_FILE_NAME = "MainModule.xls";

    private ProjectModel pm;
    private Module expenseModule;
    private Module mainModule;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Parameterized.Parameter
    public boolean singleModuleMode;

    @Parameterized.Parameters(name = "singleModuleMode: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{true}, {false}});
    }

    @Before
    public void init() throws Exception {
        createExpenseModule();
        createMainModule(); // main module depends on expense module

        File rulesFolder = tempFolder.getRoot();
        ResolvingStrategy resolvingStrategy = RulesProjectResolver.loadProjectResolverFromClassPath().isRulesProject(
                rulesFolder);
        List<Module> modules = resolvingStrategy.resolveProject(rulesFolder).getModules();

        WebStudio ws = mock(WebStudio.class);
        when(ws.getSystemConfigManager()).thenReturn(new ConfigurationManager(true, null));

        pm = new ProjectModel(ws);
        pm.setSingleModuleMode(singleModuleMode);
        for (Module module : modules) {
            if (module.getName().equals("ExpenseModule")) {
                expenseModule = module;
            }
            if (module.getName().equals("MainModule")) {
                mainModule = module;
            }
        }
    }

    @Test
    public void testSetModule() throws Exception {
        // Initial field name
        pm.setModuleInfo(expenseModule);
        Method methods[] = getExpenseInstanceClass(pm).getMethods();
        assertTrue(contains(methods, "setArea"));
        assertTrue(contains(methods, "getArea"));

        // Try to change field name and reload only current module.
        // Getter/setter methods should change.
        setFieldName("dwarea");
        pm.reset(ReloadType.SINGLE);
        methods = getExpenseInstanceClass(pm).getMethods();
        assertTrue(contains(methods, "setDwarea"));
        assertTrue(contains(methods, "getDwarea"));
    }

    @Test
    public void testDependencyOwnerRebuild() throws Exception {
        // Initial field name
        pm.setModuleInfo(mainModule);
        assertFalse(pm.getCompiledOpenClass().hasErrors());

        // Try to change a field name of dependent module and reload only that
        // module.
        pm.setModuleInfo(expenseModule);
        setFieldName("dwarea");
        pm.reset(ReloadType.SINGLE);
        // Back to the main module
        pm.setModuleInfo(mainModule);
        assertTrue(pm.getCompiledOpenClass().hasErrors());

        // Try to fix the error in dependent module and reload only that module.
        pm.setModuleInfo(expenseModule);
        setFieldName("area");
        pm.reset(ReloadType.SINGLE);
        // Back to the main module
        pm.setModuleInfo(mainModule);
        assertFalse(pm.getCompiledOpenClass().hasErrors());
    }

    private boolean contains(Method methods[], String name) {
        for (Method method : methods) {
            if (method.getName().equals(name))
                return true;
        }
        return false;
    }

    private Class<?> getExpenseInstanceClass(ProjectModel pm) {
        return pm.getCompiledOpenClass().getTypes().get("org.openl.this::Expense").getInstanceClass();
    }

    private void setFieldName(String fieldName) throws IOException {
        Workbook wb = getWorkbook();
        Sheet testedSheet = wb.getSheet(SHEET_NAME);
        Row row = testedSheet.getRow(testedSheet.getFirstRowNum() + 1);
        row.getCell(1).setCellValue(fieldName);
        writeBook(wb, EXPENSE_MODULE_FILE_NAME);
    }

    private Workbook getWorkbook() {
        WorkbookSyntaxNode[] workbookNodes = pm.getWorkbookNodes();
        return workbookNodes[0].getWorkbookSourceCodeModule().getWorkbook();
    }

    private void createExpenseModule() throws IOException {
        Workbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet(SHEET_NAME);
        String expenseTable[][] = {
                {"Datatype Expense"}, 
                {"String", "area"}
        };

        createTable(sheet, expenseTable);
        writeBook(book, EXPENSE_MODULE_FILE_NAME);
    }

    private void createMainModule() throws IOException {
        Workbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet(SHEET_NAME);
        String environmentTable[][] = {
                {"Environment"}, 
                {"dependency", "ExpenseModule"}
        };
        String dataTable[][] = {
                {"Data Expense a"}, 
                {"area", "Area", "Test area"}
        };

        createTable(sheet, environmentTable);
        createTable(sheet, dataTable);
        writeBook(book, MAIN_MODULE_FILE_NAME);
    }

    private void createTable(Sheet sheet, String table[][]) {
        int firstRow = sheet.getLastRowNum() + 2;
        for (int i = 0; i < table.length; i++) {
            Row row = sheet.createRow(firstRow + i);
            for (int j = 0; j < table[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(table[i][j]);
            }
        }

    }

    private void writeBook(Workbook wb, String file) throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(new File(tempFolder.getRoot(), file)));
            wb.write(os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}
