package org.openl.rules.calc;

/*
 * #%L
 * OpenL - RuleService - RuleService - Web Services Databinding
 * %%
 * Copyright (C) 2013 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.AegisType;
import org.apache.cxf.aegis.type.TypeUtil;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.type.basic.BeanTypeInfo;
import org.apache.cxf.aegis.type.java5.Java5TypeCreator;
import org.apache.cxf.aegis.xml.MessageReader;
import org.openl.rules.ruleservice.databinding.WrapperBeanTypeInfo;
import org.openl.rules.table.Point;

/**
 * Custom mapping for {@link SpreadSheetResult} due to it is not usual bean all
 * results should be registered using the special methods.
 * 
 * This class uses Java Generics and causes one problems that is described in
 * {@link OpenLTypeMapping}.
 * 
 * @author Marat Kamalov
 */
public class SpreadSheetResultType extends BeanType {
    public static final Class<?> TYPE_CLASS = SpreadsheetResult.class;

    public static final QName QNAME = new Java5TypeCreator().createQName(TYPE_CLASS);

    public SpreadSheetResultType() {
        super(new WrapperBeanTypeInfo(TYPE_CLASS, QNAME.getNamespaceURI(), Arrays.asList(new String[] { "height",
                "width" })));
        setTypeClass(TYPE_CLASS);
        setSchemaType(QNAME);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        BeanTypeInfo inf = getTypeInfo();

        try {
            Object[][] results = null;
            String[] columnNames = null;
            String[] rowNames = null;
            Map<String, Point> fieldsCoordinates = null;
            // Read child elements
            while (reader.hasMoreElementReaders()) {
                MessageReader childReader = reader.getNextElementReader();
                if (childReader.isXsiNil()) {
                    childReader.readToEnd();
                    continue;
                }
                QName qName = childReader.getName();
                AegisType defaultType = inf.getType(qName);
                AegisType type = TypeUtil.getReadType(childReader.getXMLStreamReader(),
                    context.getGlobalContext(),
                    defaultType);
                if (type != null && qName.getLocalPart().equals("columnNames")) {
                    columnNames = (String[]) type.readObject(childReader, context);
                } else if (type != null && qName.getLocalPart().equals("rowNames")) {
                    rowNames = (String[]) type.readObject(childReader, context);
                } else if (type != null && qName.getLocalPart().equals("fieldsCoordinates")) {
                    fieldsCoordinates = (Map<String, Point>) type.readObject(childReader, context);
                } else if (type != null && qName.getLocalPart().equals("results")) {
                    results = (Object[][]) type.readObject(childReader, context);
                } else {
                    childReader.readToEnd();
                }
            }

            return new SpreadsheetResult(results, rowNames, columnNames, fieldsCoordinates);
        } catch (IllegalArgumentException e) {
            throw new DatabindingException("Illegal argument. " + e.getMessage(), e);
        }
    }
}
