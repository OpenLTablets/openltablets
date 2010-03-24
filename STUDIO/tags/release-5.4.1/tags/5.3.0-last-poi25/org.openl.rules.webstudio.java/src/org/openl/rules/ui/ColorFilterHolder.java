/**
 * Created Mar 5, 2007
 */
package org.openl.rules.ui;

import org.openl.rules.table.ui.GreyColorFilter;
import org.openl.rules.table.ui.IColorFilter;
import org.openl.rules.table.ui.TransparentColorFilter;

/**
 * @author snshor
 *
 */
public class ColorFilterHolder {

    public static String[] filterNames = { "Red Filter", "Green Filter", "Blue Filter", "Grey Filter" };

    public static String[] imageNames = { "blend_red.png", "blend_green.png", "blend_blue.png", "blend_grey.png" };

    int transparency = 80;
    int filterType = 3;

    public String getFilterName() {
        return filterNames[filterType];
    }

    public int getFilterType() {
        return filterType;
    }

    public String getImageName() {
        return imageNames[filterType];
    }

    public int getTransparency() {
        return transparency;
    }

    public IColorFilter makeFilter() {
        double trcy = transparency * 0.01;
        switch (filterType) {
            case 0:
                return new TransparentColorFilter(IColorFilter.RED, trcy);
            case 1:
                return new TransparentColorFilter(IColorFilter.GREEN, trcy);
            case 2:
                return new TransparentColorFilter(IColorFilter.BLUE, trcy);
            case 3:
                return new GreyColorFilter(trcy);
            default:
                throw new RuntimeException("Unknown Filter Type: " + filterType);
        }
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public void setTransparency(int transparency) {
        this.transparency = Math.max(0, Math.min(100, transparency));
    }

}
