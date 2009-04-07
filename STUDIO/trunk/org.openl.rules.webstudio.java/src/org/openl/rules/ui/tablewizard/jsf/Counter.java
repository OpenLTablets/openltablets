package org.openl.rules.ui.tablewizard.jsf;

/**
 * Managed bean class that allows JSF pages use a counter object.
 *
 * @author Aliaksandr Antonik.
 */
public class Counter {
    private int count;

    public int getCount() {
        return count++;
    }

    public int getCurrent() {
        return count;
    }

    public int getPrev() {
        return count - 1;
    }

    /**
     * Resets counter. The method is named in this way to allow JSF pages call
     * it so: <code>#{counter.reset}</code>
     *
     * @return <code>true</code>
     */
    public boolean isReset() {
        count = 0;
        return true;
    }
}
