/**
 *
 */
package org.openl.rules.tbasic.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.BoundError;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.engine.OpenLManager;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.Algorithm;
import org.openl.rules.tbasic.AlgorithmSubroutineMethod;
import org.openl.rules.tbasic.AlgorithmTreeNode;
import org.openl.rules.tbasic.NoParamMethodField;
import org.openl.source.IOpenSourceCodeModule;
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
    private CompileContext mainCompileContext;
    private List<AlgorithmFunctionCompiler> functions = new ArrayList<AlgorithmFunctionCompiler>();
    private LabelManager labelManager;

    /***************************************************************************
     * Compiler output
     **************************************************************************/
    private ModuleOpenClass thisTargetClass;

    private IBindingContext thisContext;

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

    private void compile(List<AlgorithmTreeNode> nodesToProcess) throws Exception {
        for (AlgorithmFunctionCompiler functionCompiler : functions) {
            functionCompiler.compile();
        }
    }

    private void createAlgorithmInternalMethod(List<AlgorithmTreeNode> nodesToCompile, IOpenClass returnType,
            CompileContext methodContext) throws BoundError {
        // method name will be at every label
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
        }
        Map<String, AlgorithmTreeNode> internalLablesOfMethod = AlgorithmCompilerTool
                .getAllDeclaredLables(nodesToCompile);
        methodContext.registerGroupOfLabels(internalLablesOfMethod);
    }

    private IBindingContext createBindingContext() {
        if (thisContext == null) {
            thisContext = new ModuleBindingContext(context, thisTargetClass);
        }
        return thisContext;
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
        createAlgorithmInternalMethod(nodesToCompile, returnType, new CompileContext());

    }

    private void declareSubroutine(List<AlgorithmTreeNode> nodesToCompile) throws BoundError {
        CompileContext subroutineContext = new CompileContext();
        // add all labels from main
        subroutineContext.registerGroupOfLabels(mainCompileContext.getExistingLables());

        createAlgorithmInternalMethod(nodesToCompile, JavaOpenClass.VOID, subroutineContext);
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

    private IOpenClass discoverFunctionType(List<AlgorithmTreeNode> children, String returnValueInstruction)
            throws BoundError {
        // find first RETURN operation
        List<AlgorithmTreeNode> returnNodes = findFirstReturn(children);

        if (returnNodes == null || returnNodes.size() == 0) {
            StringValue lastAction = AlgorithmCompilerTool.getLastExecutableOperation(children).getAlgorithmRow()
                    .getAction();
            return getTypeOfField(lastAction);
        } else {
            // get RETURN.condition part of instruction
            String fieldWithOpenLStatement = "RETURN.condition"; // returnValueInstruction
            return getTypeOfField(AlgorithmCompilerTool.getCellContent(returnNodes, fieldWithOpenLStatement));
        }
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

    private String generateOpenClassName() {
        return header.getName();
    }

    public LabelManager getLabelManager() {
        return labelManager;
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

    public IOpenClass getThisTargetClass() {
        return thisTargetClass;
    }

    public IOpenClass getTypeOfField(StringValue fieldContent) {
        // TODO: make rational type detecting(without creating of
        // CompositeMethod)
        IOpenSourceCodeModule src = fieldContent.asSourceCodeModule();
        OpenL openl = context.getOpenL();
        IMethodSignature signature = header.getSignature();
        IBindingContext cxt = createBindingContext();
        IOpenClass filedType = OpenLManager.makeMethodWithUnknownType(openl, src, "cell_" + fieldContent.getValue(),
                signature, thisTargetClass, cxt).getMethod().getType();
        return filedType;
    }

    private void initialization(Algorithm algorithm) throws BoundError {
        labelManager = new LabelManager();
        thisTargetClass = new ModuleOpenClass(null, generateOpenClassName(), context.getOpenL());

        initNewInternalVariable("ERROR", getTypeOfField(new StringValue("new RuntimeException()")));
        initNewInternalVariable("Error Message", getTypeOfField(new StringValue("\"Error!\"")));

        mainCompileContext = new CompileContext();
        List<AlgorithmTreeNode> mainFunction = getMainFunctionBody();
        mainCompileContext.registerGroupOfLabels(AlgorithmCompilerTool.getAllDeclaredLables(mainFunction));
        functions.add(new AlgorithmFunctionCompiler(mainFunction, mainCompileContext, algorithm, this));
    }

    private void initNewInternalVariable(String variableName, IOpenClass variableType) {
        IOpenField field = new DynamicObjectField(thisTargetClass, variableName, variableType);
        thisTargetClass.addField(field);
    }

    public IMethodCaller makeMethod(IOpenSourceCodeModule src, String methodName) {
        OpenL openl = context.getOpenL();
        IMethodSignature signature = header.getSignature();
        IBindingContext cxt = createBindingContext();
        return OpenLManager.makeMethodWithUnknownType(openl, src, methodName, signature, thisTargetClass, cxt);
    }

    private void postprocess(Algorithm algorithm) {
        for (AlgorithmFunctionCompiler functionCompiler : functions) {
            functionCompiler.postprocess();
        }
        algorithm.setThisClass(getThisTargetClass());
    }

    /***************************************************************************
     * Main precompile, compile, postprocess logic
     **************************************************************************/

    private void precompile(List<AlgorithmTreeNode> nodesToProcess) throws BoundError {
        precompileNestedNodes(nodesToProcess);
    }

    private void precompileLinkedNodesGroup(List<AlgorithmTreeNode> nodesToCompile) throws BoundError {
        assert nodesToCompile.size() > 0;

        ConversionRuleBean conversionRule = ConversionRulesController.getInstance().getConvertionRule(nodesToCompile);

        for (ConversionRuleStep convertionStep : conversionRule.getConvertionSteps()) {
            preprocessConversionStep(nodesToCompile, convertionStep);
        }
    }

    private void precompileNestedNodes(List<AlgorithmTreeNode> nodesToProcess) throws BoundError {
        // process nodes by groups of linked nodes
        for (int i = 0, linkedNodesGroupSize; i < nodesToProcess.size(); i += linkedNodesGroupSize) {
            linkedNodesGroupSize = AlgorithmCompilerTool.getLinkedNodesGroupSize(nodesToProcess, i);

            List<AlgorithmTreeNode> nodesToCompile = nodesToProcess.subList(i, i + linkedNodesGroupSize);

            precompileLinkedNodesGroup(nodesToCompile);
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
        if (!operationType.startsWith("!") || operationType.equals("!CheckLabel")) {
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

}
