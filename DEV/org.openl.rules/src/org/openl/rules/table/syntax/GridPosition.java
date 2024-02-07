/*
 * Created on Oct 7, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.table.syntax;

import org.openl.rules.table.IGrid;
import org.openl.util.text.IPosition;
import org.openl.util.text.TextInfo;

/**
 * @author snshor
 */
public class GridPosition implements IPosition {

    private final int x, y;

    private final String uri;

    GridPosition(int x, int y, IGrid grid) {
        this.x = x;
        this.y = y;
        this.uri = grid.getCell(x, y).getUri();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.util.text.IPosition#getAbsolutePosition(org.openl.util.text.TextInfo)
     */
    @Override
    public int getAbsolutePosition(TextInfo info) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.util.text.IPosition#getColumn(org.openl.util.text.TextInfo, int)
     */
    @Override
    public int getColumn(TextInfo info) {
        return x;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.util.text.IPosition#getLine(org.openl.util.text.TextInfo)
     */
    @Override
    public int getLine(TextInfo info) {
        return y;
    }

    @Override
    public String toString() {
        return uri;
    }

}
