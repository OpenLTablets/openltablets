package org.openl.engine;

import org.openl.IOpenSourceCodeModule;
import org.openl.IOpenVM;
import org.openl.OpenL;
import org.openl.OpenlUtils;
import org.openl.ProcessedCode;
import org.openl.SourceType;
import org.openl.binding.IBoundCode;
import org.openl.binding.IBoundMethodNode;
import org.openl.binding.IBoundNode;
import org.openl.binding.MethodNotFoundException;
import org.openl.binding.OpenLRuntimeException;
import org.openl.binding.impl.LiteralBoundNode;
import org.openl.syntax.SyntaxErrorException;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;

/**
 * Class that defines OpenL engine manager implementation for evaluate/run
 * operations.
 * 
 */
public class OpenlRunManager extends BaseOpenlManager {

    private OpenlSourceManager sourceManager;
    private OpenlCompileManager compileManager;

    /**
     * Creates new instance of OpenL engine manager.
     * 
     * @param openl {@link OpneL} instance
     */
    public OpenlRunManager(OpenL openl) {
        super(openl);
        sourceManager = new OpenlSourceManager(openl);
        compileManager = new OpenlCompileManager(openl);
    }

    /**
     * Compiles and runs specified method.
     * 
     * @param source source
     * @param methodName method name
     * @param paramTypes parameters types
     * @param params parameters values
     * @return result of method execution
     * @throws OpenLRuntimeException
     * @throws MethodNotFoundException
     * @throws SyntaxErrorException
     */
    public Object runMethod(IOpenSourceCodeModule source, String methodName, IOpenClass[] paramTypes, Object[] params)
            throws OpenLRuntimeException, MethodNotFoundException, SyntaxErrorException {

        IOpenClass openClass = compileManager.compileModule(source);
        IOpenVM vm = getOpenL().getVm();

        Object target = openClass.newInstance(vm.getRuntimeEnv());

        IOpenMethod method = OpenlUtils.getMethod(methodName, paramTypes, openClass);

        return method.invoke(target, params, vm.getRuntimeEnv());
    }

    /**
     * Compiles and runs OpenL script.
     * 
     * @param source source
     * @return result of script execution
     * @throws OpenLRuntimeException
     */
    public Object runScript(IOpenSourceCodeModule source) throws OpenLRuntimeException {

        return run(source, SourceType.METHOD_BODY);
    }

    /**
     * Compiles source and runs code.
     * 
     * @param source source
     * @param sourceType type of source
     * @return result of execution
     * @throws OpenLRuntimeException
     */
    public Object run(IOpenSourceCodeModule source, SourceType sourceType) throws OpenLRuntimeException {

        ProcessedCode processedCode = sourceManager.processSource(source, sourceType);

        IBoundCode boundCode = processedCode.getBoundCode();
        IBoundNode boundNode = boundCode.getTopNode();

        IOpenVM vm = getOpenL().getVm();

        if (boundNode instanceof IBoundMethodNode) {
            return vm.getRunner().run((IBoundMethodNode) boundNode, new Object[0]);
        }

        if (boundNode instanceof LiteralBoundNode) {
            return ((LiteralBoundNode) boundNode).getValue();
        }

        try {
            throw new Exception("Unrunnable Bound Node Type:" + boundNode.getClass().getName());
        } catch (Exception ex) {
            throw new OpenLRuntimeException(ex, boundNode);
        }
    }

}