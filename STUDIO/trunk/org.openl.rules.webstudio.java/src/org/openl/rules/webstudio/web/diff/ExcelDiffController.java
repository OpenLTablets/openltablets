package org.openl.rules.webstudio.web.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.exception.OpenLRuntimeException;
import org.openl.rules.diff.tree.DiffTreeNode;
import org.openl.rules.diff.xls2.XlsDiff2;

public class ExcelDiffController extends AbstractDiffController {

    /**
     * Max files count to compare.
     */
    protected static final int MAX_FILES_COUNT = 2;

    private List<File> filesToCompare = new ArrayList<File>();

    public List<File> getFilesToCompare() {
        return filesToCompare;
    }

    public void setFilesToCompare(List<File> filesToCompare) {
        this.filesToCompare = filesToCompare;
    }

    public String compare() {
        if (filesToCompare.size() >= MAX_FILES_COUNT) {
            File file1 = filesToCompare.get(0);
            File file2 = filesToCompare.get(1);
            filesToCompare.clear();

            XlsDiff2 x = new XlsDiff2();
            try {
                DiffTreeNode diffTree = x.diffFiles(
                        file1.getAbsolutePath(), file2.getAbsolutePath());
                setDiffTree(diffTree);
            } catch (OpenLRuntimeException e) {
                FacesUtils.addInfoMessage(e.getMessage());
            }

        }

        return null;
    }

    public void compare(List<File> files) {
        setFilesToCompare(files);
        compare();
    }

}
