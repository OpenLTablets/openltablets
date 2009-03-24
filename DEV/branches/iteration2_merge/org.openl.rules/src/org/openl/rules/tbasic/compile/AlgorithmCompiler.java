/**
 * 
 */
package org.openl.rules.tbasic.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.IOpenSourceCodeModule;
import org.openl.OpenL;
import org.openl.OpenlTool;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.BoundError;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.Algorithm;
import org.openl.rules.tbasic.AlgorithmSubroutineMethod;
import org.openl.rules.tbasic.AlgorithmTreeNode;
import org.openl.rules.tbasic.NoParamMethodField;
import org.openl.types.IMethodCaller;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.DynamicObjectField;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.java.JavaOpenClass;

// FIXME: !!!!!!!!!!!!!!! refactor to eliminate code duplications and to isolate
// different functionality in separate classes

/**
 * @author User
 * 
 */
public class AlgorithmCompiler {
    /***************************************************************************
     * Initial data
     **************************************************************************/
    private IBindingContext context;
    private IOpenMethodHeader header;
    private List<AlgorithmTreeNode> nodesToCompile;

    /***************************************************************************
     * Intermediate values
     **************************************************************************/
    private List<AlgorithmFunctionCompiler> functions = new ArrayList<AlgorithmFunctionCompiler>();
    private LabelManager labelManager;

    /***************************************************************************
     * Compiler output
     **************************************************************************/
    private ModuleOpenClass thisTargetClass;
    private CompileContext mainCompileContext;
    private Map<String, CompileContext> internalMethodsContexts;

    public IOpenClass getThisTargetClass() {
        return thisTargetClass;
    }

    public CompileContext getMainCompileContext() {
        return mainCompileContext;
    }

    public Map<String, CompileContext> getInternalMethodsContexts() {
        return internalMethodsContexts;
    }

    public LabelManager getLabelManager() {
        return labelManager;
    }

    public AlgorithmCompiler(IBindingContext context, IOpenMethodHeader header, List<AlgorithmTreeNode> nodesToCompile) {
        this.context = context;
        this.header = header;
        this.nodesToCompile = nodesToCompile;
    }

    /***************************************************************************
     * Main logic
     **************************************************************************/

    public void compile(Algorithm algorithm) throws Exception {
        initialization(algorithm);
        precompile(nodesToCompile);
        compile(nodesToCompile);
        postprocess(algorithm);
    }

    private void initialization(Algorithm algorithm) {
        labelManager = new LabelManager();
        mainCompileContext = new CompileContext();
        internalMethodsContexts = new HashMap<String, CompileContext>();
        thisTargetClass = new ModuleOpenClass(null, generateOpenClassName(), context.getOpenL());

        initNewInternalVariable("ERROR", getTypeOfField(new StringValue("new RuntimeException()")));
        initNewInternalVariable("Error Message", getTypeOfField(new StringValue("\"Error!\"")));
        // add main function of Algorithm
        functions.add(new AlgorithmFunctionCompiler(getMainFunctionBody(), mainCompileContext, algorithm, this));
    }

    /***************************************************************************
     * Main precompile, compile, postprocess logic
     **************************************************************************/

    private void precompile(List<AlgorithmTreeNode> nodesToProcess) throws BoundError {
        precompileNestedNodes(nodesToProcess);
    }

    private void precompileNestedNodes(List<AlgorithmTreeNode> nodesToProcess) throws BoundError {
        // process nodes by groups of linked nodes
        for (int i = 0, linkedNodesGroupSize; i < nodesToProcess.size(); i += linkedNodesGroupSize) {
            linkedNodesGroupSize = AlgorithmCompilerTool.getLinkedNodesGroupSize(nodesToProcess, i);

            List<AlgorithmTreeNode> nodesToCompile = nodesToProcess.subList(i, i + linkedNodesGroupSize);

            precompileLinkedNodesGroup(nodesToCompile);
        }
    }

    private void precompileLinkedNodesGroup(List<AlgorithmTreeNode> nodesToCompile) throws BoundError {
        assert nodesToCompile.size() > 0;

        ConversionRuleBean conversionRule = ConversionRulesController.getInstance().getConvertionRule(nodesToCompile);

        for (ConversionRuleStep convertionStep : conversionRule.getConvertionSteps()) {
            preprocessConversionStep(nodesToCompile, convertionStep);
        }
    }

    /**
     * @param nodesToCompile
     * @param conversionRule
     * @throws BoundError
     */
    private void preprocessConversionStep(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep conversionStep)
            throws BoundError {
        assert nodesToCompile.size() > 0;
        assert conversionStep != null;

        String operationType = conversionStep.getOperationType();
        // TODO
        if (!operationType.startsWith("!")) {
            // do nothing
        } else if (operationType.equals("!Compile")) {
            List<AlgorithmTreeNode> nodesToProcess;
            nodesToProcess = AlgorithmCompilerTool.getNestedInstructionsBlock(nodesToCompile, conversionStep
                    .getOperationParam1());
            precompileNestedNodes(nodesToProcess);
        } else if (operationType.equals("!Declare")) {
            declareVariable(nodesToCompile, conversionStep);
        } else if (operationType.equals("!Subroutine")) {
            declareSubroutine(nodesToCompile);
        } else if (operationType.equals("!Function")) {
            declareFunction(nodesToCompile, conversionStep);
        } else {
            IOpenSourceCodeModule errorSource = nodesToCompile.get(0).getAlgorithmRow().getOperation()
                    .asSourceCodeModule();
            throw new BoundError(String.format("Unknown compilation instruction %s", operationType), errorSource);
        }
    }

    private void compile(List<AlgorithmTreeNode> nodesToProcess) throws Exception {
        for (AlgorithmFunctionCompiler functionCompiler : functions) {
            functionCompiler.compile();
        }
    }

    private void postprocess(Algorithm algorithm) {
        for (AlgorithmFunctionCompiler functionCompiler : functions) {
            functionCompiler.postprocess();
        }
        algorithm.setThisClass(getThisTargetClass());
    }

    /***************************************************************************
     * Helper methods
     **************************************************************************/

    private List<AlgorithmTreeNode> getMainFunctionBody() {
        int currentOperationIndex = 0;
        while (currentOperationIndex < nodesToCompile.size()
                && !nodesToCompile.get(currentOperationIndex).getSpecification().getKeyword().equals("FUNCTION")
                && !nodesToCompile.get(currentOperationIndex).getSpecification().getKeyword().equals("SUB")) {
            currentOperationIndex++;
        }
        return nodesToCompile.subList(0, currentOperationIndex);
    }

    public IOpenClass getTypeOfField(StringValue fieldContent) {
        // TODO: make rational type detecting(without creating of
        // CompositeMethod)
        IOpenSourceCodeModule src = fieldContent.asSourceCodeModule();
        OpenL openl = context.getOpenL();
        IMethodSignature signature = header.getSignature();
        IBindingContext cxt = createBindingContext();
        IOpenClass filedType = OpenlTool.makeMethodWithUnknownType(src, openl, "cell_" + fieldContent.getValue(),
                signature, thisTargetClass, cxt).getMethod().getType();
        return filedType;
    }

    public IMethodCaller makeMethod(IOpenSourceCodeModule src, String methodName) {
        OpenL openl = context.getOpenL();
        IMethodSignature signature = header.getSignature();
        IBindingContext cxt = createBindingContext();
        return OpenlTool.makeMethodWithUnknownType(src, openl, methodName, signature, thisTargetClass, cxt);
    }

    private void initNewInternalVariable(String variableName, IOpenClass variableType) {
        IOpenField field = new DynamicObjectField(thisTargetClass, variableName, variableType);
        thisTargetClass.addField(field);
    }

    private void declareSubroutine(List<AlgorithmTreeNode> nodesToCompile) throws BoundError {
        createAlgorithmInternalMethod(nodesToCompile, JavaOpenClass.VOID);
    }

    private void declareFunction(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep convertionStep)
            throws BoundError {
        String returnValueInstruction = convertionStep.getOperationParam1();

        IOpenClass returnType = JavaOpenClass.VOID;
        if (AlgorithmCompilerTool.isOperationFieldInstruction(returnValueInstruction)) {
            returnType = getTypeOfField(AlgorithmCompilerTool.getCellContent(nodesToCompile, returnValueInstruction));
        } else {
            // TODO add support of specification instruction
            returnType = discoverFunctionType(nodesToCompile.get(0).getChildren(), returnValueInstruction);
        }
        createAlgorithmInternalMethod(nodesToCompile, returnType);

    }

    private IOpenClass discoverFunctionType(List<AlgorithmTreeNode> children, String returnValueInstruction)
            throws BoundError {
        // find first RETURN operation
        List<AlgorithmTreeNode> returnNodes = findFirstReturn(children);

        //TODO processing function without RETURN
        if (returnNodes == null || returnNodes.size() == 0){
            IOpenSourceCodeModule errorSource = children.get(children.size() - 1).getAlgorithmRow().getOperation()
                    .asSourceCodeModule();
            throw new BoundError("RETURN operation expected.", errorSource);
        }

        // get RETURN.condition part of instruction
        String fieldWithOpenLStatement = "RETURN.condition"; // returnValueInstruction
        return getTypeOfField(AlgorithmCompilerTool.getCellContent(returnNodes, fieldWithOpenLStatement));
    }

    private List<AlgorithmTreeNode> findFirstReturn(List<AlgorithmTreeNode> nodes) {
        // FIXME delete this method at all
        List<AlgorithmTreeNode> returnNodeSubList = null;
        for (int i = 0; i < nodes.size() && returnNodeSubList == null; i++) {
            if (nodes.get(i).getSpecification().getKeyword().equals("RETURN")) {
                returnNodeSubList = nodes.subList(i, i + 1);
            } else if (nodes.get(i).getChildren() != null) {
                returnNodeSubList = findFirstReturn(nodes.get(i).getChildren());
            }
        }
        return returnNodeSubList;
    }

    private void createAlgorithmInternalMethod(List<AlgorithmTreeNode> nodesToCompile, IOpenClass returnType) {
        // method name will be at every label
        CompileContext methodContext = new CompileContext();
        for (StringValue label : nodesToCompile.get(0).getLabels()) {
            String methodName = label.getValue();
            IOpenMethodHeader methodHeader = new OpenMethodHeader(methodName, returnType, IMethodSignature.VOID,
                    thisTargetClass);

            AlgorithmSubroutineMethod method = new AlgorithmSubroutineMethod(methodHeader);

            thisTargetClass.addMethod(method);

            // to support parameters free call
            NoParamMethodField methodAlternative = new NoParamMethodField(methodName, method);
            thisTargetClass.addField(methodAlternative);

            functions.add(new AlgorithmFunctionCompiler(nodesToCompile, methodContext, method, this));
            internalMethodsContexts.put(methodName, methodContext);
        }
    }

    private void declareVariable(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep conversionStep)
            throws BoundError {
        String variableNameParameter = conversionStep.getOperationParam1();
        String variableAssignmentParameter = conversionStep.getOperationParam2();
        StringValue variableName = AlgorithmCompilerTool.getCellContent(nodesToCompile, variableNameParameter);
        IOpenClass variableType = getTypeOfField(AlgorithmCompilerTool.getCellContent(nodesToCompile,
                variableAssignmentParameter));
        initNewInternalVariable(variableName.getValue(), variableType);
    }

    private String generateOpenClassName() {
        return header.getName();
    }

    private IBindingContext thisContext;

    private IBindingContext createBindingContext() {
        if (thisContext == null) {
            thisContext = new ModuleBindingContext(context, thisTargetClass);
        }
        return thisContext;
    }

}
