/**
 * 
 */
package org.openl.rules.tbasic.compile;

import org.openl.rules.table.IGridRegion;
import org.openl.rules.tbasic.AlgorithmTreeNode;

/**
 * @author User
 * 
 */
public class AlgorithmOperationSource {
    private AlgorithmTreeNode sourceNode;
    private String operationFieldName;

    public AlgorithmOperationSource(AlgorithmTreeNode sourceNode, String operationFieldName) {
        this.sourceNode = sourceNode;
        this.operationFieldName = operationFieldName;
    }

    public String getSourceUri() {
        return sourceNode.getAlgorithmRow().getOperation().asSourceCodeModule().getUri(0);
    }

    public IGridRegion getGridRegion() {
        IGridRegion sourceRegion = null;

        if (operationFieldName != null) {
            sourceRegion = sourceNode.getAlgorithmRow().getValueGridRegion(operationFieldName);
        }

        // if source is not value source or not found
        if (sourceRegion == null) {
            sourceRegion = sourceNode.getAlgorithmRow().getGridRegion();
        }

        return sourceRegion;
    }

    public String getOperationName() {
        return sourceNode.getAlgorithmRow().getOperation().getValue();
    }

    public int getRowNumber() {
        return sourceNode.getAlgorithmRow().getRowNumber();
    }
}
