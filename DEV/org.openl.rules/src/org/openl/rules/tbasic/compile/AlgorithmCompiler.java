/**
 *
 */
package org.openl.rules.tbasic.compile;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.component.ComponentBindingContext;
import org.openl.binding.impl.component.ComponentOpenClass;
import org.openl.engine.OpenLCellExpressionsCompiler;
import org.openl.meta.StringValue;
import org.openl.rules.tbasic.Algorithm;
import org.openl.rules.tbasic.AlgorithmSubroutineMethod;
import org.openl.rules.tbasic.AlgorithmTreeNode;
import org.openl.rules.tbasic.NoParamMethodField;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.types.*;
import org.openl.types.impl.DynamicObjectField;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.java.JavaOpenClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private ComponentOpenClass thisTargetClass;

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
            CompileContext methodContext) throws SyntaxNodeException {
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

    private IBindingContext getAlgorithmBindingContext() {
        if (thisContext == null) {
            thisContext = new ComponentBindingContext(context, thisTargetClass);
        }
        return thisContext;
    }

    private void declareFunction(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep convertionStep)
            throws SyntaxNodeException {
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

    private void declareSubroutine(List<AlgorithmTreeNode> nodesToCompile) throws SyntaxNodeException {
        CompileContext subroutineContext = new CompileContext();
        // add all labels from main
        subroutineContext.registerGroupOfLabels(mainCompileContext.getExistingLables());

        createAlgorithmInternalMethod(nodesToCompile, JavaOpenClass.VOID, subroutineContext);
    }

    private void declareVariable(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep conversionStep)
            throws SyntaxNodeException {
        String variableNameParameter = conversionStep.getOperationParam1();
        String variableAssignmentParameter = conversionStep.getOperationParam2();
        StringValue variableName = AlgorithmCompilerTool.getCellContent(nodesToCompile, variableNameParameter);
        IOpenClass variableType = getTypeOfField(AlgorithmCompilerTool.getCellContent(nodesToCompile,
                variableAssignmentParameter));
        initNewInternalVariable(variableName.getValue(), variableType);
    }

    /**
     * Find out the type of the array element. And define the internal variable
     */
    private void declareArrayElement(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep conversionStep)
            throws SyntaxNodeException {
        // Points to the location of the elementName in the TBasic table
        //
        String elementNameParameter = conversionStep.getOperationParam1();

        // Points to the location of the iterable array parameter in the Tbasic table
        //
        String iterableArrayParameter = conversionStep.getOperationParam2();

        // Extract the element name
        //
        StringValue elementName = AlgorithmCompilerTool.getCellContent(nodesToCompile, elementNameParameter);

        // Extract the type of the iterable array
        //
        IOpenClass iterableArrayType = getTypeOfField(AlgorithmCompilerTool.getCellContent(nodesToCompile,
                iterableArrayParameter));
        if (!iterableArrayType.isArray()) {
            IOpenSourceCodeModule errorSource = nodesToCompile.get(0).getAlgorithmRow().getAction()
                    .asSourceCodeModule();
            throw SyntaxNodeExceptionUtils.createError(String.format("Compilation failure. The cell should be of the array type", elementName), errorSource);
        }
        IOpenClass elementType = iterableArrayType.getComponentClass();
        initNewInternalVariable(elementName.getValue(), elementType);
    }

    private IOpenClass discoverFunctionType(List<AlgorithmTreeNode> children, String returnValueInstruction)
            throws SyntaxNodeException {
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
        return OpenLCellExpressionsCompiler.makeMethodWithUnknownType(openl, src, "cell_" + fieldContent.getValue(),
                signature, thisTargetClass, getAlgorithmBindingContext())
                    .getMethod()
                    .getType();
    }

    private void initialization(Algorithm algorithm) throws SyntaxNodeException {
        labelManager = new LabelManager();
        thisTargetClass = new ComponentOpenClass(null, generateOpenClassName(), context.getOpenL());

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
        IBindingContext cxt = getAlgorithmBindingContext();
        return OpenLCellExpressionsCompiler.makeMethodWithUnknownType(openl, src, methodName, signature, thisTargetClass, cxt);
    }
    
    public IMethodCaller makeMethodWithCast(IOpenSourceCodeModule src, String methodName, IOpenClass returnType) {
        OpenL openl = context.getOpenL();      
        IMethodSignature signature = header.getSignature();
        // create method header for newly created method
        OpenMethodHeader header = new OpenMethodHeader(methodName, returnType, signature, thisTargetClass);
        
        IBindingContext cxt = getAlgorithmBindingContext();
        return OpenLCellExpressionsCompiler.makeMethod(openl, src, header, cxt);
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

    private void precompile(List<AlgorithmTreeNode> nodesToProcess) throws SyntaxNodeException {
        precompileNestedNodes(nodesToProcess);
    }

    private void precompileLinkedNodesGroup(List<AlgorithmTreeNode> nodesToCompile) throws SyntaxNodeException {
        assert nodesToCompile.size() > 0;

        ConversionRuleBean conversionRule = ConversionRulesController.getInstance().getConvertionRule(nodesToCompile);

        for (ConversionRuleStep convertionStep : conversionRule.getConvertionSteps()) {
            preprocessConversionStep(nodesToCompile, convertionStep);
        }
    }

    private void precompileNestedNodes(List<AlgorithmTreeNode> nodesToProcess) throws SyntaxNodeException {
        // process nodes by groups of linked nodes
        for (int i = 0, linkedNodesGroupSize; i < nodesToProcess.size(); i += linkedNodesGroupSize) {
            linkedNodesGroupSize = AlgorithmCompilerTool.getLinkedNodesGroupSize(nodesToProcess, i);

            List<AlgorithmTreeNode> nodesToCompile = nodesToProcess.subList(i, i + linkedNodesGroupSize);

            precompileLinkedNodesGroup(nodesToCompile);
        }
    }

    private void preprocessConversionStep(List<AlgorithmTreeNode> nodesToCompile, ConversionRuleStep conversionStep)
            throws SyntaxNodeException {
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
        } else if (operationType.equals("!DeclareArrayElement")) {
            declareArrayElement(nodesToCompile, conversionStep);
        } else if (operationType.equals("!Subroutine")) {
            declareSubroutine(nodesToCompile);
        } else if (operationType.equals("!Function")) {
            declareFunction(nodesToCompile, conversionStep);
        } else {
            IOpenSourceCodeModule errorSource = nodesToCompile.get(0).getAlgorithmRow().getOperation()
                    .asSourceCodeModule();
            throw SyntaxNodeExceptionUtils.createError(String.format("Unknown compilation instruction %s", operationType), errorSource);
        }
    }

}
