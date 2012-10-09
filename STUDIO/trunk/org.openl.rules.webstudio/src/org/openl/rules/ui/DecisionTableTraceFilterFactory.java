package org.openl.rules.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openl.rules.dt.trace.DTConditionTraceObject;
import org.openl.rules.dt.trace.DTIndexedTraceObject;
import org.openl.rules.dt.trace.DTRuleTracerLeaf;
import org.openl.rules.dt.trace.DecisionTableTraceObject;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ITableTracerObject;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.ui.RegionGridSelector;
import org.openl.rules.table.ui.filters.CellStyleGridFilter;
import org.openl.rules.table.ui.filters.ColorGridFilter;
import org.openl.rules.table.ui.filters.FontGridFilter;
import org.openl.rules.table.ui.filters.IColorFilter;
import org.openl.rules.table.ui.filters.IGridFilter;
import org.openl.util.tree.ITreeElement;
import org.openl.vm.trace.ITracerObject;

public class DecisionTableTraceFilterFactory {
    private static final int SELECTED_ITEM_INCREMENT_SIZE = 3;
    
    private final ITableTracerObject selectedTraceObject;
    private final IColorFilter defaultColorFilter;
    
    private List<IGridRegion> successfulChecks = new ArrayList<IGridRegion>();
    private List<IGridRegion> unsuccessfulChecks = new ArrayList<IGridRegion>();
    private List<IGridRegion> resultRegions = new ArrayList<IGridRegion>();
    private List<IGridRegion> allCheckedRegions = new ArrayList<IGridRegion>();
    private List<IGridRegion> selectedRegions;
    private List<IGridRegion> indexedRegions = new ArrayList<IGridRegion>();

    public DecisionTableTraceFilterFactory(ITableTracerObject selectedTraceObject, IColorFilter defaultColorFilter) {
        this.selectedTraceObject = selectedTraceObject;
        this.defaultColorFilter = defaultColorFilter;
        this.selectedRegions = selectedTraceObject.getGridRegions();
    }

    public IGridFilter[] createFilters() {
        ITableTracerObject rootTraceObject = getRoot();

        fillRegions(rootTraceObject);

        List<IGridFilter> filters = buildFilters();

        return filters.toArray(new IGridFilter[filters.size()]);
    }

    private ITableTracerObject getRoot() {
        ITableTracerObject rootTraceObject = selectedTraceObject;
        while (rootTraceObject.getParent() != null && rootTraceObject.getParent() instanceof DecisionTableTraceObject) {
            rootTraceObject = (ITableTracerObject) rootTraceObject.getParent();
        }
        return rootTraceObject;
    }

    private void fillRegions(ITableTracerObject rootTraceObject) {
        Iterator<? extends ITreeElement<ITracerObject>> children;
        if (rootTraceObject instanceof DecisionTableTraceObject) {
            children = ((DecisionTableTraceObject) rootTraceObject).getTraceConditions();
        } else {
            children = rootTraceObject.getChildren();
        }
        for (Iterator<?> iterator = children; iterator.hasNext();) {
            ITableTracerObject child = (ITableTracerObject) iterator.next();
            List<IGridRegion> regions = child.getGridRegions();

            if (child instanceof DTConditionTraceObject) {
                DTConditionTraceObject conditionTrace = (DTConditionTraceObject) child;
                if (child instanceof DTIndexedTraceObject) {
                    indexedRegions.addAll(regions);
                }
                if (conditionTrace.isSuccessful()) {
                    successfulChecks.addAll(regions);
                    allCheckedRegions.addAll(regions);
                } else {
                    unsuccessfulChecks.addAll(regions);
                    allCheckedRegions.addAll(regions);
                }

                fillRegions(conditionTrace);
            } else if (child instanceof DTRuleTracerLeaf) {
                resultRegions.addAll(regions);
                allCheckedRegions.addAll(regions);

                successfulChecks.removeAll(regions);
                unsuccessfulChecks.removeAll(regions);
            }
        }
    }

    private List<IGridFilter> buildFilters() {
        short[] resultColor = new short[] { 0, 0xaa, 0 };

        FontGridFilter successfulFontFilter = new FontGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(successfulChecks), false))
                .setFontColor(resultColor)
                .setItalic(true)
                .setBold(false)
                .build();
        FontGridFilter unsuccessfulFontFilter = new FontGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(unsuccessfulChecks), false))
                .setFontColor(IColorFilter.RED)
                .setItalic(true)
                .setBold(false)
                .build();
        FontGridFilter resultFontFilter = new FontGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(resultRegions), false))
                .setFontColor(resultColor)
                .setItalic(false)
                .setBold(true)
                .build();
        FontGridFilter indexedFontFilter = new FontGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(indexedRegions), false))
                .setUnderlined(true)
                .build();

        CellStyleGridFilter notResultBorderFilter = new CellStyleGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(resultRegions), true))
                .setBorderStyle(ICellStyle.BORDER_DOTTED)
                .build();
        CellStyleGridFilter resultBorderFilter = new CellStyleGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(resultRegions), false))
                .setBorderStyle(ICellStyle.BORDER_THICK)
                .setBorderRGB(resultColor)
                .build();
        
        List<IGridFilter> filters = new ArrayList<IGridFilter>();
        filters.add(successfulFontFilter);
        filters.add(unsuccessfulFontFilter);
        filters.add(resultFontFilter);
        filters.add(indexedFontFilter);
        filters.add(notResultBorderFilter);
        filters.add(resultBorderFilter);
        filters.add(resultBorderFilter.createUpperRowBorderFilter());
        filters.add(resultBorderFilter.createLefterColumnBorderFilter());

        if (selectedRegions != null) {
            FontGridFilter selectedFontFilter = new FontGridFilter.Builder()
                .setSelector(new RegionGridSelector(toArray(selectedRegions), false))
                .setIncrementSize(SELECTED_ITEM_INCREMENT_SIZE)
                //.setFontColor(IColorFilter.WHITE)
                .build();
            filters.add(selectedFontFilter);
        }

        filters.add(createColorFilter(toArray(allCheckedRegions), IColorFilter.WHITE, ColorGridFilter.BACKGROUND));
        //if (selectedRegions != null) {
            //filters.add(createColorFilter(toArray(selectedRegions), resultColor, ColorGridFilter.BACKGROUND));
        //}
        filters.add(new ColorGridFilter(new RegionGridSelector(toArray(allCheckedRegions), true), defaultColorFilter));

        return filters;
    }

    private IGridRegion[] toArray(Collection<IGridRegion> regions) {
        return regions.toArray(new IGridRegion[regions.size()]);
    }

    private ColorGridFilter createColorFilter(IGridRegion[] region, final short[] rewriteColor, int scope) {
        IColorFilter colorFilter = new IColorFilter() {

            @Override
            public short[] filterColor(short[] color) {
                return rewriteColor;
            }
        };

        return new ColorGridFilter(new RegionGridSelector(region, false), colorFilter, scope);
    }
    
}
