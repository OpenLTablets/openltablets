package org.openl.binding.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openl.binding.IBoundNode;
import org.openl.binding.MethodUtil;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.types.impl.MatchingOpenMethodDispatcher;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.ExecutableMethod;
import org.openl.types.impl.MethodDelegator;
import org.openl.util.text.ILocation;
import org.openl.util.text.TextInfo;

/**
 * Helps to find all used OpenL methods in compiled code by {@link IBoundNode}.
 * 
 * @author PUdalau
 */
public class MethodUsagesSearcher {

    public static class MethodUsage {
        private int startPos;
        private int endPos;
        private IOpenMethod method;

        public MethodUsage(int startPos, int endPos, IOpenMethod method) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.method = method;
        }

        /**
         * @return the start position of the method in code. 
         */
        public int getStart() {
            return startPos;
        }

        /**
         * @return the end position of the method in code. 
         */
        public int getEnd() {
            return endPos;
        }

        public IOpenMethod getMethod() {
            return method;
        }

        /**
         * 
         * @return uri of the table representing used method or
         *         <code>null</code> if this method is not represented by some
         *         OpenL component.
         */
        public String getTableUri() {
            try {
                if (method instanceof ExecutableRulesMethod) {
                    return ((ExecutableRulesMethod) method).getSyntaxNode().getUri();
                } else if (method instanceof MatchingOpenMethodDispatcher) {
                    return ((MatchingOpenMethodDispatcher) method).getDispatcherTable().getUri();
                } else if (method.getInfo() != null) {
                    return method.getInfo().getSourceUrl();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return String description of the method signature.
         */
        public String getMethodSignature() {
            StringBuilder buff = new StringBuilder();
            MethodUtil.printMethod(method, buff);
            return buff.toString();
        }
    }

    /**
     * Find all OpenL methods used in the code.
     * 
     * @param boundNode Compiled OpenL expression.
     * @param sourceString Source of OpenL expression.
     * @param startIndex Position in the <code>sourceString</code> which defines
     *            start of OpenL expression.
     * @return
     */
    public static List<MethodUsage> findAllMethods(IBoundNode boundNode, String sourceString, int startIndex) {
        List<MethodUsage> methods = new ArrayList<MethodUsage>();
        findAllMethods(boundNode, methods, sourceString, startIndex);
        return methods;
    }

    private static void findAllMethods(IBoundNode boundNode, List<MethodUsage> methods, String sourceString,
            int startIndex) {
        if (boundNode instanceof MethodBoundNode) {
            MethodBoundNode methodBoundNode = (MethodBoundNode) boundNode;
            ILocation location = methodBoundNode.getSyntaxNode().getSourceLocation();
            IMethodCaller methodCaller = methodBoundNode.getMethodCaller();
            if (methodCaller != null) {
                IOpenMethod method;
                if(methodCaller instanceof IOpenMethod){
                    method= (IOpenMethod)methodCaller;
                }else{
                    method= methodCaller.getMethod();
                }
                int pstart = 0;
                int pend = 0;
                if ((method instanceof ExecutableMethod || method instanceof MatchingOpenMethodDispatcher || method instanceof MethodDelegator)
                        && location != null && location.isTextLocation()) {
                    TextInfo info = new TextInfo(sourceString);
                    pstart = location.getStart().getAbsolutePosition(info) + startIndex;
                    pend = pstart + method.getName().length() - 1;
                    methods.add(new MethodUsage(pstart, pend, method));
                }
            }
        }
        if (ArrayUtils.isNotEmpty(boundNode.getChildren())) {
            for (IBoundNode child : boundNode.getChildren()) {
                findAllMethods(child, methods, sourceString, startIndex);
            }
        }
        if(boundNode instanceof ATargetBoundNode){
            IBoundNode targetNode = ((ATargetBoundNode)boundNode).getTargetNode();
            if (targetNode != null) {
                findAllMethods(targetNode, methods, sourceString, startIndex);
            }
        }
    }
}
