package org.openl.rules.webstudio.web.trace;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.table.ATableTracerNode;
import org.openl.rules.table.IGridRegion;
import org.openl.rules.table.ITableTracerObject;
import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.types.IParameterDeclaration;
import org.openl.util.tree.ITreeElement;
import org.openl.vm.trace.ITracerObject;

import java.util.List;

public class TracerObjectDecorator implements ITableTracerObject {

    private ITableTracerObject tracerObject;

    public TracerObjectDecorator(ITableTracerObject tracerObject) {
        this.tracerObject = tracerObject;
    }

    public Iterable<? extends ITreeElement<ITracerObject>> getChildren() {
        if (tracerObject != null) {
            return tracerObject.getChildren();
        }
        return null;
    }

    public ITracerObject getObject() {
        if (tracerObject != null) {
            return tracerObject.getObject();
        }
        return null;
    }

    public String getType() {
        if (tracerObject != null) {
            return tracerObject.getType();
        }
        return null;
    }

    public boolean isLeaf() {
        if (tracerObject != null) {
            return tracerObject.isLeaf();
        }
        return false;
    }

    public List<IGridRegion> getGridRegions() {
        if (tracerObject != null) {
            return tracerObject.getGridRegions();
        }
        return null;
    }

    public ITracerObject getParent() {
        if (tracerObject != null) {
            return tracerObject.getParent();
        }
        return null;
    }

    public TableSyntaxNode getTableSyntaxNode() {
        if (tracerObject != null) {
            return tracerObject.getTableSyntaxNode();
        }
        return null;
    }

    public ITableTracerObject[] getTableTracers() {
        if (tracerObject != null) {
            return tracerObject.getTableTracers();
        }
        return null;
    }

    public void setParent(ITracerObject parentTraceObject) {
        if (tracerObject != null) {
            tracerObject.setParent(parentTraceObject);
        }
    }

    public void addChild(ITracerObject child) {
        if (tracerObject != null) {
            tracerObject.addChild(child);
        }
    }

    public ITracerObject[] getTracerObjects() {
        if (tracerObject != null) {
            return tracerObject.getTracerObjects();
        }
        return null;
    }

    public Object getTraceObject() {
        if (tracerObject != null) {
            return tracerObject.getTraceObject();
        }
        return null;
    }

    public String getUri() {
        if (tracerObject != null) {
            return tracerObject.getUri();
        }
        return null;
    }

    public String getDisplayName(int mode) {
        if (tracerObject != null) {
            return tracerObject.getDisplayName(mode);
        }
        return null;
    }

    public String getName() {
        if (tracerObject != null) {
            return tracerObject.getName();
        }
        return null;
    }

    public Object getResult() {
        if (tracerObject != null) {
            return tracerObject.getResult();
        }
        return null;
    }

    public ParameterWithValueDeclaration[] getInputParameters() {
        if (tracerObject != null) {
            if (tracerObject instanceof ATableTracerNode) {
                return getInputParameters((ATableTracerNode) tracerObject);
            } else if (tracerObject.getParent() instanceof ATableTracerNode) {
                // ATableTracerLeaf
                return getInputParameters((ATableTracerNode) tracerObject.getParent());
            }
        }
        return null;
    }

    private ParameterWithValueDeclaration[] getInputParameters(ATableTracerNode tracerNode) {
        Object[] parameters = tracerNode.getParameters();
        if (tracerNode.getTraceObject() instanceof ExecutableRulesMethod) {
            ExecutableRulesMethod tracedMethod = (ExecutableRulesMethod) tracerNode.getTraceObject();
            ParameterWithValueDeclaration[] paramDescriptions = new ParameterWithValueDeclaration[parameters.length];
            for (int i = 0; i < paramDescriptions.length; i++) {
                paramDescriptions[i] = new ParameterWithValueDeclaration(tracedMethod.getSignature().getParameterName(i),
                        parameters[i], IParameterDeclaration.IN);
            }
            return paramDescriptions;
        } else {
            return null;
        }
    }

    public ParameterWithValueDeclaration getReturnResult() {
        ParameterWithValueDeclaration returnResult = null;
        Object result = getResult();
        if (result != null) {
            returnResult = new ParameterWithValueDeclaration("return", result, IParameterDeclaration.OUT);
        }
        return returnResult;
    }
}
