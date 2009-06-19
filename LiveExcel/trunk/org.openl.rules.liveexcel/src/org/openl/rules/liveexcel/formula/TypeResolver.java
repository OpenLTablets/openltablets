package org.openl.rules.liveexcel.formula;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.openl.rules.liveexcel.LiveExcelException;
import org.openl.rules.liveexcel.ServiceModelAPI;


/**
 * Resolve the cell value as native Java class for Openl
 *
 */
public class TypeResolver {
    
    /** Pattern to find a number format: "0" or  "#" */
    private static final Pattern numPattern = Pattern.compile("[0#]+");
    
    /** Pattern to find a number format: "0" or  "#" */
    private static final Pattern textPattern = Pattern.compile("@");   
    
    private Cell cell;
    private ServiceModelAPI serviceModel;
    
    public TypeResolver(Cell cell, ServiceModelAPI serviceModel) {
        this.cell = cell;
        this.serviceModel = serviceModel;
    }
    
    
    public static Class<?> resolveType(Cell cell, ServiceModelAPI serviceModel) {
        TypeResolver resolver = new TypeResolver(cell, serviceModel);
        Class<?> result = null;
        try{
            if(resolver.isCellServiceModel()){        
                result = resolver.resolveServiceModel();
            } else {
                if(resolver.isFormatDefined()) {
                    result = resolver.resolveByFormat();
                } else {
                    if(resolver.isCellFormula()) {            
                        result = resolver.resolveByValue(cell.getCachedFormulaResultType());
                    } else {
                        result = resolver.resolveByValue(cell.getCellType());
                    }
                }
            }
        } catch (LiveExcelException e) {
            e.printStackTrace();
            return Object.class;
        }
        return result;
    }
    
    private Class<?> resolveServiceModel() {        
        return serviceModel.getServiceModelObjectDomainType(getMatchServiceModelName());
    }

    private boolean isCellServiceModel() {
        boolean res = false;
        if(isCellFormula()&&getMatchServiceModelName()!=null) {
            res = true;            
       }
        return res;
    }
    
    private String getMatchServiceModelName() {
        String res = null;
        for(String servModUDF : serviceModel.getAllServiceModelUDFs()) {
            if(cell.getCellFormula().startsWith(servModUDF))
                res = servModUDF;
        }
        return res;
    }

    /**
     * Resolve the type by the value in the cell
     * 
     * @return the value of the cell in native Java type
     */
    private Class<?> resolveByValue(int type) {
        Class<?> result = null;                
        switch (type) {
            case HSSFCell.CELL_TYPE_NUMERIC: {
                double d = cell.getNumericCellValue();               
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    result = HSSFDateUtil.getJavaDate(d).getClass();
                } else {
                    result = ((Double)cell.getNumericCellValue()).getClass();
                }
                break;
            }
            case HSSFCell.CELL_TYPE_STRING: {
                result = cell.getRichStringCellValue().getString().getClass();
                break;
            }
            case HSSFCell.CELL_TYPE_BOOLEAN: {
                result = ((Boolean)cell.getBooleanCellValue()).getClass();
                break;
            }
            case HSSFCell.CELL_TYPE_ERROR: {                
                throw new LiveExcelException("Type of cell is ERROR. Not supported");                
            }   
        }
        return result;
    }
    
    /**
     * Check if cell type is formula
     */
    private boolean isCellFormula() {
        boolean res = false;
        if(cell.getCellType()==HSSFCell.CELL_TYPE_FORMULA) {
            res = true;
        }
        return res;
    }
    
    /**
     * Resolve the type by format of the cell
     * 
     * @return the value of the cell in native Java type
     */
    private Class<?> resolveByFormat() {
        Class<?> result = null;
        CellStyle style = cell.getCellStyle();
        int dataFormat = style.getDataFormat();
        String dataFormatStr = style.getDataFormatString();
        if(DateUtil.isADateFormat(dataFormat, dataFormatStr)) {
            result = Date.class;
        } else {
            if(numPattern.matcher(dataFormatStr).find()) {
                result = Double.class;
            } else {
                if(textPattern.matcher(dataFormatStr).find()) {
                    result = String.class;
                } 
            }
        }        
        return result;
    }
    
    /**
     * Check if format is defined (Not General)
     */
    private boolean isFormatDefined() {
        boolean res = false;
        CellStyle style = cell.getCellStyle();        
        if(!"General".equals(style.getDataFormatString())) {
            res = true;
        } 
        return res;
    }
}
