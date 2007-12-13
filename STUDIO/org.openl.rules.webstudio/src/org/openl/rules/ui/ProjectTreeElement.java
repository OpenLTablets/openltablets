package org.openl.rules.ui;

import org.openl.base.INamedThing;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;

import org.openl.util.TreeElement;

import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * @author Stanislav Shor
 */
public class ProjectTreeElement extends TreeElement implements INamedThing {
    String url;
    String[] displayName;
    int nameCount = 0;
    TableSyntaxNode tsn;
    Object problem;

    public ProjectTreeElement(String[] displayName, String type, String url,
        Object problems, int nameCount, TableSyntaxNode tsn) {
        super(type);
        this.url = url;
        this.displayName = displayName;
        this.problem = problems;
        this.nameCount = nameCount;
        this.tsn = tsn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return getDisplayName(SHORT);
    }

    public String getDisplayName(int mode) {
        return //"<img src='images/test.gif'/>" +
        displayName[mode];
    }

    public String[] getDisplayName() {
        return this.displayName;
    }

    public boolean hasProblem() {
        if (problem != null) {
            return true;
        }

        for (Iterator iter = this.getChildren(); iter.hasNext();) {
            ProjectTreeElement pt = (ProjectTreeElement) iter.next();
            if (pt.hasProblem()) {
                return true;
            }
        }

        return false;
    }

    public Object getProblem() {
        return this.problem;
    }

    public void setProblem(Object problem) {
        this.problem = problem;
    }

    public int getNameCount() {
        return this.nameCount;
    }
}
