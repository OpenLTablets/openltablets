package org.openl.rules.ui;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tika.io.IOUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.resolving.ProjectResolvingException;
import org.openl.rules.project.resolving.ResolvingStrategy;
import org.openl.rules.project.resolving.RulesProjectResolver;

import java.io.*;
import java.util.List;

public abstract class AbstractWorkbookGeneratingTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected List<Module> getModules() throws ProjectResolvingException {
        File rulesFolder = tempFolder.getRoot();
        ResolvingStrategy resolvingStrategy = RulesProjectResolver.loadProjectResolverFromClassPath().isRulesProject(
                rulesFolder);
        return resolvingStrategy.resolveProject(rulesFolder).getModules();
    }

    protected void createTable(Sheet sheet, String table[][]) {
        int firstRow = sheet.getLastRowNum() + 2;
        for (int i = 0; i < table.length; i++) {
            Row row = sheet.createRow(firstRow + i);
            for (int j = 0; j < table[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(table[i][j]);
            }
        }

    }

    protected void writeBook(Workbook wb, String file) throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(new File(tempFolder.getRoot(), file)));
            wb.write(os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}
