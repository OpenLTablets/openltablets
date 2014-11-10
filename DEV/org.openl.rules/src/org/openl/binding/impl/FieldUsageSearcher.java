package org.openl.binding.impl;

import java.util.List;

import org.openl.binding.IBoundNode;
import org.openl.meta.IMetaInfo;
import org.openl.rules.data.DataOpenField;
import org.openl.rules.lang.xls.binding.XlsModuleOpenClass;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.util.text.ILocation;
import org.openl.util.text.TextInfo;

public final class FieldUsageSearcher {
    private static final SimpleTypeConverter TYPE_CONVERTER = new SimpleTypeConverter();

    private FieldUsageSearcher() {
    }

    public static void findAllFields(List<NodeUsage> fields,
            IBoundNode boundNode, String sourceString,
            int startPosition) {
        if (boundNode instanceof FieldBoundNode) {
            if (boundNode.getTargetNode() != null) {
                findAllFields(fields, boundNode.getTargetNode(), sourceString, startPosition);
            }

            TextInfo tableHeaderText = new TextInfo(sourceString);
            IOpenField boundField = ((FieldBoundNode) boundNode).getBoundField();
            IOpenClass type = boundField.getDeclaringClass();
            if (type == null) {
                return;
            }

            if (type instanceof XlsModuleOpenClass) {
                if (boundField instanceof DataOpenField) {
                    fields.add(createDataTableFieldUsage(boundNode,
                            startPosition,
                            tableHeaderText,
                            (DataOpenField) boundField));
                }
            } else {
                SimpleNodeUsage simpleNodeUsage = createFieldOfDatatype(boundNode,
                        startPosition,
                        tableHeaderText,
                        type);
                if (simpleNodeUsage != null) {
                    fields.add(simpleNodeUsage);
                }
            }
        } else if (boundNode instanceof IndexNode) {
            findAllFields(fields, boundNode.getTargetNode(), sourceString, startPosition);
        } else {
            if (boundNode.getChildren() == null) {
                return;
            }
            for (IBoundNode child : boundNode.getChildren()) {
                findAllFields(fields, child, sourceString, startPosition);
            }
        }
    }

    public static SimpleNodeUsage createFieldOfDatatype(IBoundNode boundNode,
            int startPosition,
            TextInfo tableHeaderText, IOpenClass type) {
        IMetaInfo metaInfo = type.getMetaInfo();
        while (metaInfo == null && type.isArray()) {
            type = type.getComponentClass();
            metaInfo = type.getMetaInfo();
        }
        ILocation typeLocation = boundNode.getSyntaxNode().getSourceLocation();
        SimpleNodeUsage simpleNodeUsage = null;
        if (metaInfo != null && typeLocation != null) {
            int start = startPosition + typeLocation.getStart().getAbsolutePosition(tableHeaderText);
            int end = startPosition + typeLocation.getEnd().getAbsolutePosition(tableHeaderText);
            String description = TYPE_CONVERTER.convert(type) + "\n" +
                    TYPE_CONVERTER.convert(boundNode.getType()) + " " + ((FieldBoundNode) boundNode).getFieldName();
            simpleNodeUsage = new SimpleNodeUsage(start,
                    end,
                    description,
                    metaInfo.getSourceUrl(),
                    NodeType.FIELD);
        }
        return simpleNodeUsage;
    }

    public static TableUsage createDataTableFieldUsage(IBoundNode boundNode,
            int startPosition,
            TextInfo tableHeaderText,
            DataOpenField boundField) {
        ILocation typeLocation = boundNode.getSyntaxNode().getSourceLocation();
        int start = startPosition + typeLocation.getStart().getAbsolutePosition(tableHeaderText);
        int end = startPosition + typeLocation.getEnd().getAbsolutePosition(tableHeaderText);
        return new TableUsage(boundField.getTable(), start, end, NodeType.FIELD);
    }
}
