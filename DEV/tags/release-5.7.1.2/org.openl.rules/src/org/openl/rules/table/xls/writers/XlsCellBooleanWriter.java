package org.openl.rules.table.xls.writers;

import org.openl.rules.table.xls.XlsSheetGridModel;

public class XlsCellBooleanWriter extends AXlsCellWriter {

    public XlsCellBooleanWriter(XlsSheetGridModel xlsSheetGridModel) {
        super(xlsSheetGridModel);        
    }

    @Override
    public void writeCellValue(boolean writeMetaInfo) {        
        Boolean boolValue = (Boolean) getValueToWrite();
        getCellToWrite().setCellValue(boolValue.booleanValue());        

        if (writeMetaInfo) {
            setMetaInfo(Boolean.class);
        }
    }
}
