package org.openl.rules.dt2.algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.IllegalSelectorException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.impl.BinaryOpNode;
import org.openl.binding.impl.BinaryOpNodeAnd;
import org.openl.binding.impl.BlockNode;
import org.openl.binding.impl.FieldBoundNode;
import org.openl.binding.impl.IndexNode;
import org.openl.binding.impl.LiteralBoundNode;
import org.openl.binding.impl.cast.IOpenCast;
import org.openl.meta.BigDecimalValue;
import org.openl.meta.BigIntegerValue;
import org.openl.meta.ByteValue;
import org.openl.meta.DoubleValue;
import org.openl.meta.FloatValue;
import org.openl.meta.IntValue;
import org.openl.meta.LongValue;
import org.openl.meta.ShortValue;
import org.openl.rules.dt2.algorithm.evaluator.EqualsIndexedEvaluator;
import org.openl.rules.dt2.algorithm.evaluator.IConditionEvaluator;
import org.openl.rules.dt2.algorithm.evaluator.RangeIndexedEvaluator;
import org.openl.rules.dt2.element.ICondition;
import org.openl.rules.dt2.type.IRangeAdaptor;
import org.openl.rules.dt2.type.ITypeAdaptor;
import org.openl.rules.dtx.IBaseCondition;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.ParameterDeclaration;

public class DependentParametersOptimizedAlgorithm {

    public static IConditionEvaluator makeEvaluator(ICondition condition,
            IMethodSignature signature,
            IBindingContext bindingContext) throws SyntaxNodeException {

        EvaluatorFactory evaluatorFactory = determineOptimizedEvaluationFactory(condition, signature);

        if (evaluatorFactory == null)
            return null;

        IOpenClass expressionType = evaluatorFactory.getExpressionType();

        IParameterDeclaration[] params = condition.getParams();

        switch (params.length) {

            case 1:
                IOpenClass paramType = params[0].getType();

                IOpenCast openCast = bindingContext.getCast(paramType, expressionType);

                if (openCast == null) {
                    String message = String.format("Can not convert from '%s' to '%s'. incompatible types comparasion in '%s' condition",
                        paramType.getName(),
                        expressionType.getName(), condition.getName());
                    
                    throw new SyntaxNodeException(message, null, null, condition.getSourceCodeModule());
                }

                if (evaluatorFactory instanceof OneParameterEqualsFactory) {
                    return getOneParamEqualsEvaluator(evaluatorFactory, openCast);
                } else {
                    return getOneParamRangeEvaluator(evaluatorFactory, expressionType, openCast);
                }

            case 2:

                IOpenClass paramType0 = params[0].getType();
                IOpenClass paramType1 = params[1].getType();

                if (paramType0.equals(paramType1)) {
                    IOpenCast cast = bindingContext.getCast(paramType0, expressionType);
                    if (cast == null) {
                        String message = String.format("Can not convert from '%s' to '%s'. incompatible types comparasion in '%s' condition",
                            paramType0.getName(),
                            expressionType.getName(), condition.getName());
                        throw new SyntaxNodeException(message, null, null, condition.getSourceCodeModule());
                    }
                    return getTwoParamRangeEvaluator(evaluatorFactory, expressionType, cast);
                }
                break;
        }

        return null;
    }

    private static IConditionEvaluator getTwoParamRangeEvaluator(EvaluatorFactory evaluatorFactory,
            IOpenClass paramType,
            IOpenCast openCast) {
        IRangeAdaptor<? extends Object, ? extends Comparable<?>> adaptor = getRangeAdaptor(evaluatorFactory,
            paramType,
            openCast);

        if (adaptor == null)
            return null;

        @SuppressWarnings("unchecked")
        RangeIndexedEvaluator rix = new RangeIndexedEvaluator((IRangeAdaptor<Object, ? extends Comparable<Object>>) adaptor,
            2);

        rix.setOptimizedSourceCode(evaluatorFactory.getExpression());

        return rix;
    }

    private static IConditionEvaluator getOneParamEqualsEvaluator(EvaluatorFactory evaluatorFactory, IOpenCast openCast) {
        OneParameterEqualsFactory oneParameterEqualsFactory = (OneParameterEqualsFactory) evaluatorFactory;
        OneParameterEqualsIndexedEvaluator oneParameterEqualsIndexedEvaluator = new OneParameterEqualsIndexedEvaluator(oneParameterEqualsFactory,
            openCast);
        return oneParameterEqualsIndexedEvaluator;
    }

    private static IConditionEvaluator getOneParamRangeEvaluator(EvaluatorFactory evaluatorFactory,
            IOpenClass paramType,
            IOpenCast openCast) {

        IRangeAdaptor<? extends Object, ? extends Comparable<?>> adaptor = getRangeAdaptor(evaluatorFactory,
            paramType,
            openCast);

        if (adaptor == null)
            return null;

        @SuppressWarnings("unchecked")
        RangeIndexedEvaluator rix = new RangeIndexedEvaluator((IRangeAdaptor<Object, ? extends Comparable<Object>>) adaptor,
            1);

        rix.setOptimizedSourceCode(evaluatorFactory.getExpression());

        return rix;
    }

    private static IRangeAdaptor<? extends Object, ? extends Comparable<?>> getRangeAdaptor(EvaluatorFactory evaluatorFactory,
            IOpenClass paramType,
            IOpenCast openCast) {

        Class<?> typeClass = paramType.getInstanceClass();
        if (typeClass.equals(String.class)) {
            return new RelationRangeAdaptor<String>(evaluatorFactory, ITypeAdaptor.STRING, openCast);
        }

        if (typeClass.equals(byte.class) || typeClass.equals(Byte.class)) {
            return new RelationRangeAdaptor<Byte>(evaluatorFactory, ITypeAdaptor.BYTE, openCast);
        }

        if (typeClass.equals(ByteValue.class)) {
            return new RelationRangeAdaptor<Byte>(evaluatorFactory, ITypeAdaptor.BYTE_VALUE, openCast);
        }

        if (typeClass.equals(short.class) || typeClass.equals(Short.class)) {
            return new RelationRangeAdaptor<Short>(evaluatorFactory, ITypeAdaptor.SHORT, openCast);
        }

        if (typeClass.equals(ShortValue.class)) {
            return new RelationRangeAdaptor<Short>(evaluatorFactory, ITypeAdaptor.SHORT_VALUE, openCast);
        }

        if (typeClass.equals(int.class) || typeClass.equals(Integer.class)) {
            return new RelationRangeAdaptor<Integer>(evaluatorFactory, ITypeAdaptor.INT, openCast);
        }

        if (typeClass.equals(IntValue.class)) {
            return new RelationRangeAdaptor<Integer>(evaluatorFactory, ITypeAdaptor.INT_VALUE, openCast);
        }

        if (typeClass.equals(long.class) || typeClass.equals(Long.class)) {
            return new RelationRangeAdaptor<Long>(evaluatorFactory, ITypeAdaptor.LONG, openCast);
        }

        if (typeClass.equals(LongValue.class)) {
            return new RelationRangeAdaptor<Long>(evaluatorFactory, ITypeAdaptor.LONG_VALUE, openCast);
        }

        if (typeClass.equals(double.class) || typeClass.equals(Double.class)) {
            return new RelationRangeAdaptor<Double>(evaluatorFactory, ITypeAdaptor.DOUBLE, openCast);
        }

        if (typeClass.equals(DoubleValue.class)) {
            return new RelationRangeAdaptor<Double>(evaluatorFactory, ITypeAdaptor.DOUBLE_VALUE, openCast);
        }

        if (typeClass.equals(float.class) || typeClass.equals(Float.class)) {
            return new RelationRangeAdaptor<Float>(evaluatorFactory, ITypeAdaptor.FLOAT, openCast);
        }

        if (typeClass.equals(FloatValue.class)) {
            return new RelationRangeAdaptor<Float>(evaluatorFactory, ITypeAdaptor.FLOAT_VALUE, openCast);
        }

        if (typeClass.equals(BigInteger.class)) {
            return new RelationRangeAdaptor<BigInteger>(evaluatorFactory, ITypeAdaptor.BIGINTEGER, openCast);
        }

        if (typeClass.equals(BigIntegerValue.class)) {
            return new RelationRangeAdaptor<BigInteger>(evaluatorFactory, ITypeAdaptor.BIGINTEGER_VALUE, openCast);
        }

        if (typeClass.equals(BigDecimal.class)) {
            return new RelationRangeAdaptor<BigDecimal>(evaluatorFactory, ITypeAdaptor.BIGDECIMAL, openCast);
        }

        if (typeClass.equals(BigDecimalValue.class)) {
            return new RelationRangeAdaptor<BigDecimal>(evaluatorFactory, ITypeAdaptor.BIGDECIMAL_VALUE, openCast);
        }

        if (typeClass.equals(Date.class)) {
            return new RelationRangeAdaptor<Integer>(evaluatorFactory, ITypeAdaptor.DATE, openCast);
        }

        return null;
    }

    static class RelationRangeAdaptor<C extends Comparable<C>> implements IRangeAdaptor<Object, C> {
        EvaluatorFactory evaluatorFactory;
        ITypeAdaptor<Object, C> typeAdaptor;
        IOpenCast openCast;

        @SuppressWarnings("unchecked")
        public RelationRangeAdaptor(EvaluatorFactory evaluatorFactory,
                ITypeAdaptor<? extends Object, C> typeAdaptor,
                IOpenCast openCast) {
            super();
            this.evaluatorFactory = evaluatorFactory;
            this.typeAdaptor = (ITypeAdaptor<Object, C>) typeAdaptor;
            this.openCast = openCast;
        }

        public RelationRangeAdaptor(EvaluatorFactory evaluatorFactory, ITypeAdaptor<Object, C> typeAdaptor) {
            this(evaluatorFactory, typeAdaptor, null);
        }

        @Override
        public C getMax(Object param) {
            if (evaluatorFactory.hasMax()) {
                if (openCast != null) {
                    param = openCast.convert(param);
                }
                C v = typeAdaptor.convert(param);
                if (evaluatorFactory.needsIncrement(Bound.UPPER))
                    v = typeAdaptor.increment(v);
                return v;
            }

            return null;
        }

        @Override
        public C getMin(Object param) {
            if (evaluatorFactory.hasMin()) {
                if (openCast != null) {
                    param = openCast.convert(param);
                }
                C v = typeAdaptor.convert(param);
                if (evaluatorFactory.needsIncrement(Bound.LOWER))
                    v = typeAdaptor.increment(v);
                return v;
            }

            return null;
        }

        @Override
        public C adaptValueType(Object value) {
            return typeAdaptor.convert(value);
        }

        @Override
        public boolean useOriginalSource() {
            return true;
        }

    }

    private static String buildFieldName(IndexNode indexNode) {
        String value = "[";
        if (indexNode.getChildren().length == 1 && indexNode.getChildren()[0] instanceof LiteralBoundNode) {
            LiteralBoundNode literalBoundNode = (LiteralBoundNode) indexNode.getChildren()[0];
            value = value + literalBoundNode.getValue().toString() + "]";
        } else {
            throw new IllegalSelectorException();
        }

        if (indexNode.getTargetNode() != null) {
            if (indexNode.getTargetNode() instanceof FieldBoundNode) {
                return buildFieldName((FieldBoundNode) indexNode.getTargetNode()) + value;
            }
            if (indexNode.getTargetNode() instanceof IndexNode) {
                return value + buildFieldName((IndexNode) indexNode.getTargetNode());
            }
            throw new IllegalStateException();
        }
        return value;
    }

    private static String buildFieldName(FieldBoundNode field) {
        String value = field.getFieldName();
        if (field.getTargetNode() != null) {
            if (field.getTargetNode() instanceof FieldBoundNode) {
                return buildFieldName((FieldBoundNode) field.getTargetNode()) + "." + value;
            }
            if (field.getTargetNode() instanceof IndexNode) {
                return buildFieldName((IndexNode) field.getTargetNode()) + "." + value;
            }
            throw new IllegalStateException();
        }
        return value;
    }

    private static String[] parseBinaryOpExpression(BinaryOpNode binaryOpNode) {
        if (binaryOpNode.getChildren().length == 2 && binaryOpNode.getChildren()[0] instanceof FieldBoundNode && binaryOpNode.getChildren()[1] instanceof FieldBoundNode) {
            String[] ret = new String[3];
            if (binaryOpNode.getSyntaxNode().getType().endsWith("ge")) {
                ret[1] = ">=";
            }
            if (binaryOpNode.getSyntaxNode().getType().endsWith("gt")) {
                ret[1] = ">";
            }
            if (binaryOpNode.getSyntaxNode().getType().endsWith("le")) {
                ret[1] = "<=";
            }
            if (binaryOpNode.getSyntaxNode().getType().endsWith("lt")) {
                ret[1] = "<";
            }
            if (binaryOpNode.getSyntaxNode().getType().endsWith("eq")) {
                ret[1] = "==";
            }
            if (ret[1] == null) {
                return null;
            }
            FieldBoundNode fieldBoundNode0 = (FieldBoundNode) binaryOpNode.getChildren()[0];
            FieldBoundNode fieldBoundNode1 = (FieldBoundNode) binaryOpNode.getChildren()[1];

            ret[0] = buildFieldName(fieldBoundNode0);
            ret[2] = buildFieldName(fieldBoundNode1);
            return ret;
        }
        return null;
    }

    private static String[] oneParameterExpressionParse(ICondition condition) {
        if (condition.getMethod() instanceof CompositeMethod) {
            IBoundNode boundNode = ((CompositeMethod) condition.getMethod()).getMethodBodyBoundNode();
            if (boundNode instanceof BlockNode) {
                BlockNode blockNode = (BlockNode) boundNode;
                if (blockNode.getChildren().length == 1 && blockNode.getChildren()[0] instanceof BlockNode) {
                    blockNode = (BlockNode) blockNode.getChildren()[0];
                    if (blockNode.getChildren().length == 1 && blockNode.getChildren()[0] instanceof BinaryOpNode) {
                        BinaryOpNode binaryOpNode = (BinaryOpNode) blockNode.getChildren()[0];
                        return parseBinaryOpExpression(binaryOpNode);
                    }
                }
            }
            return null;
        }
        throw new IllegalStateException("Condition method should be an instance of CompositeMethod!");
    }

    private static String[][] twoParameterExpressionParse(ICondition condition) {
        if (condition.getMethod() instanceof CompositeMethod) {
            IBoundNode boundNode = ((CompositeMethod) condition.getMethod()).getMethodBodyBoundNode();
            if (boundNode instanceof BlockNode) {
                BlockNode blockNode = (BlockNode) boundNode;
                if (blockNode.getChildren().length == 1 && blockNode.getChildren()[0] instanceof BlockNode) {
                    blockNode = (BlockNode) blockNode.getChildren()[0];
                    if (blockNode.getChildren().length == 1 && blockNode.getChildren()[0] instanceof BinaryOpNodeAnd) {
                        BinaryOpNodeAnd binaryOpNode = (BinaryOpNodeAnd) blockNode.getChildren()[0];
                        if (binaryOpNode.getChildren().length == 2 && binaryOpNode.getChildren()[0] instanceof BinaryOpNode && binaryOpNode.getChildren()[1] instanceof BinaryOpNode) {
                            BinaryOpNode binaryOpNode0 = (BinaryOpNode) binaryOpNode.getChildren()[0];
                            BinaryOpNode binaryOpNode1 = (BinaryOpNode) binaryOpNode.getChildren()[1];
                            String[] ret0 = parseBinaryOpExpression(binaryOpNode0);
                            String[] ret1 = parseBinaryOpExpression(binaryOpNode1);
                            if (ret0 != null && ret1 != null) {
                                String[][] ret = new String[2][];
                                ret[0] = ret0;
                                ret[1] = ret1;
                                return ret;
                            }
                        }
                    }
                }
            }
            return null;
        }
        throw new IllegalStateException("Condition method should be an instance of CompositeMethod!");
    }

    private static EvaluatorFactory determineOptimizedEvaluationFactory(ICondition condition, IMethodSignature signature) {
        IParameterDeclaration[] params = condition.getParams();

        String code = condition.getSourceCodeModule().getCode();
        if (code == null)
            return null;

        switch (params.length) {
            case 1:
                String[] parsedValues = oneParameterExpressionParse(condition);
                if (parsedValues == null)
                    return null;
                if ("==".equals(parsedValues[1])) {
                    return makeOneParameterEqualsFactory(parsedValues[0],
                        parsedValues[1],
                        parsedValues[2],
                        condition,
                        signature);
                } else {
                    OneParameterRangeFactory oneParameterRangefactory = makeOneParameterRangeFactory(parsedValues[0],
                        parsedValues[1],
                        parsedValues[2],
                        condition,
                        signature);
                    return oneParameterRangefactory;
                }
            case 2:
                String[][] parsedValuesTwoParameters = twoParameterExpressionParse(condition);
                if (parsedValuesTwoParameters == null)
                    return null;
                return makeTwoParameterRangeFactory(parsedValuesTwoParameters[0][0],
                    parsedValuesTwoParameters[0][1],
                    parsedValuesTwoParameters[0][2],
                    parsedValuesTwoParameters[1][0],
                    parsedValuesTwoParameters[1][1],
                    parsedValuesTwoParameters[1][2],
                    condition,
                    signature);
            default:
                return null;
        }

    }

    private static OneParameterEqualsFactory makeOneParameterEqualsFactory(String p1,
            String op,
            String p2,
            ICondition condition,
            IMethodSignature signature) {

        IParameterDeclaration signatureParam = getParameter(p1, signature);
        IParameterDeclaration conditionParam = condition.getParams()[0];

        if (signatureParam == null) {
            signatureParam = getParameter(p2, signature);
            if (signatureParam == null) {
                return null;
            }
            if (!p1.equals(conditionParam.getName()))
                return null;
            if (p2.startsWith(signatureParam.getName() + ".") || p2.equals(signatureParam.getName())){
                return new OneParameterEqualsFactory(signatureParam, p2);
            }else{
                return new OneParameterEqualsFactory(signatureParam, signatureParam.getName() + "." + p2);
            }
        }

        if (!p2.equals(conditionParam.getName()))
            return null;

        if (p1.startsWith(signatureParam.getName() + ".") || p1.equals(signatureParam.getName())){
            return new OneParameterEqualsFactory(signatureParam, p1);
        }else{
            return new OneParameterEqualsFactory(signatureParam, signatureParam.getName() + "." + p1);
        }
    }

    private static OneParameterRangeFactory makeOneParameterRangeFactory(String p1,
            String op,
            String p2,
            ICondition condition,
            IMethodSignature signature) {

        IParameterDeclaration signatureParam = getParameter(p1, signature);

        if (signatureParam == null)
            return makeOppositeOneParameterRangeFactory(p1, op, p2, condition, signature);

        IParameterDeclaration conditionParam = condition.getParams()[0];

        if (!p2.equals(conditionParam.getName()))
            return null;

        RelationType relation = RelationType.findElement(op);

        if (relation == null)
            throw new RuntimeException("Could not find relation: " + op);

        if (p1.startsWith(signatureParam.getName() + ".") || p1.equals(signatureParam.getName())){
            return new OneParameterRangeFactory(signatureParam, conditionParam, relation, p1);
        }else{
            return new OneParameterRangeFactory(signatureParam, conditionParam, relation, signatureParam.getName() + "." + p1);
        }
    }

    private static TwoParameterRangeFactory makeTwoParameterRangeFactory(String p11,
            String op1,
            String p12,
            String p21,
            String op2,
            String p22,
            ICondition condition,
            IMethodSignature signature) {

        RelationType rel1 = RelationType.findElement(op1);

        if (!rel1.isLessThan()) {
            rel1 = RelationType.findElement(rel1.opposite);
            String tmp = p11;
            p11 = p12;
            p12 = tmp;
        }

        RelationType rel2 = RelationType.findElement(op2);

        if (!rel2.isLessThan()) {
            rel2 = RelationType.findElement(rel2.opposite);
            String tmp = p21;
            p21 = p22;
            p22 = tmp;
        }

        if (p12.equals(p21))
            return makeTwoParameterRangeFactory(p11, rel1, p12, p21, rel2, p22, condition, signature);

        if (p11.equals(p22))
            return makeTwoParameterRangeFactory(p21, rel2, p22, p11, rel1, p12, condition, signature);

        return null;

    }

    private static TwoParameterRangeFactory makeTwoParameterRangeFactory(String p11,
            RelationType rel1,
            String p12,
            String p21,
            RelationType rel2,
            String p22,
            ICondition condition,
            IMethodSignature signature) {

        IParameterDeclaration signatureParam = getParameter(p12, signature);

        if (signatureParam == null)
            return null;

        IParameterDeclaration conditionParam1 = condition.getParams()[0];

        if (!p11.equals(conditionParam1.getName()))
            return null;

        IParameterDeclaration conditionParam2 = condition.getParams()[1];

        if (!p22.equals(conditionParam2.getName()))
            return null;

        if (p12.startsWith(signatureParam.getName() + ".") || p12.equals(signatureParam.getName())){
            return new TwoParameterRangeFactory(signatureParam, conditionParam1, rel1, conditionParam2, rel2, p12);
        }else{
            return new TwoParameterRangeFactory(signatureParam, conditionParam1, rel1, conditionParam2, rel2, signatureParam.getName() + "." + p12);
        }

    }

    private static IParameterDeclaration getParameter(String pname, IMethodSignature signature) {
        String parameterName = pname;
        if (pname.indexOf(".") > 0) {
            parameterName = pname.substring(0, pname.indexOf("."));
        }

        for (int i = 0; i < signature.getNumberOfParameters(); i++) {
            if (parameterName.equals(signature.getParameterName(i))) {
                return new ParameterDeclaration(signature.getParameterType(i), parameterName);
            }
        }
        
        for (int i = 0; i < signature.getNumberOfParameters(); i++) {
            if (signature.getParameterType(i).getField(pname, false) != null){
                return new ParameterDeclaration(signature.getParameterType(i), signature.getParameterName(i));
            }
        }
        
        return null;
    }

    private static OneParameterRangeFactory makeOppositeOneParameterRangeFactory(String p1,
            String op,
            String p2,
            ICondition condition,
            IMethodSignature signature) {

        IParameterDeclaration signatureParam = getParameter(p2, signature);

        if (signatureParam == null)
            return null;

        IParameterDeclaration conditionParam = condition.getParams()[0];

        if (!p1.equals(conditionParam.getName()))
            return null;

        RelationType relation = RelationType.findElement(op);

        if (relation == null)
            throw new RuntimeException("Could not find relation: " + op);

        String oppositeOp = relation.opposite;

        relation = RelationType.findElement(oppositeOp);

        if (relation == null)
            throw new RuntimeException("Could not find relation: " + oppositeOp);

        if (p2.startsWith(signatureParam.getName() + ".") || p2.equals(signatureParam.getName())){
            return new OneParameterRangeFactory(signatureParam, conditionParam, relation, p2);
        }else{
            return new OneParameterRangeFactory(signatureParam, conditionParam, relation, signatureParam.getName() + "." + p2);
        }
    }

    static class RangeEvaluatorFactory {

        public RangeEvaluatorFactory(String regex, int numberOfparams, int minDelta, int maxDelta) {
            super();
            this.regex = regex;
            this.numberOfparams = numberOfparams;
            this.minDelta = minDelta;
            this.maxDelta = maxDelta;
        }

        Pattern pattern;
        String regex;
        int numberOfparams;
        int minDelta, maxDelta;
    }

    RangeEvaluatorFactory[] rangeFactories = { new RangeEvaluatorFactory(null, 0, 0, 0) };

    enum Bound {
        LOWER,
        UPPER
    }

    public static class OneParameterEqualsIndexedEvaluator extends EqualsIndexedEvaluator {
        OneParameterEqualsFactory oneParameterEqualsFactory;

        public OneParameterEqualsIndexedEvaluator(OneParameterEqualsFactory oneParameterEqualsFactory,
                IOpenCast openCast) {
            super(openCast);
            if (oneParameterEqualsFactory == null) {
                throw new IllegalArgumentException("parameterDeclaration");
            }
            this.oneParameterEqualsFactory = oneParameterEqualsFactory;
        }

        public OneParameterEqualsIndexedEvaluator(OneParameterEqualsFactory oneParameterEqualsFactory) {
            this(oneParameterEqualsFactory, null);
        }

        @Override
        public String getOptimizedSourceCode() {
            return oneParameterEqualsFactory.getExpression();
        }

        @Override
        public IOpenSourceCodeModule getFormalSourceCode(IBaseCondition condition) {
            return condition.getSourceCodeModule();
        }
    }

    static abstract class EvaluatorFactory {

        IParameterDeclaration signatureParam;
        String expression;

        public EvaluatorFactory(IParameterDeclaration signatureParam, String expression) {
            super();
            this.signatureParam = signatureParam;
            this.expression = expression;
        }

        public abstract boolean hasMin();

        public abstract boolean hasMax();

        public abstract boolean needsIncrement(Bound bound);

        public static final String ARRAY_ACCESS_PATTERN = ".+\\[[0-9]+\\]$";

        private static IOpenClass findExpressionType(IOpenClass type, String expression) {
            StringTokenizer stringTokenizer = new StringTokenizer(expression, ".");
            boolean isFirst = true;
            while (stringTokenizer.hasMoreTokens()) {
                String v = stringTokenizer.nextToken();
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                boolean arrayAccess = v.matches(ARRAY_ACCESS_PATTERN);
                IOpenField field = null;
                if (arrayAccess) {
                    v = v.substring(0, v.indexOf("["));
                }
                field = type.getField(v);
                type = field.getType();
                if (type.isArray() && arrayAccess) {
                    type = type.getComponentClass();
                }
            }
            return type;
        }

        public String getExpression() {
            return expression;
        }

        public IOpenClass getExpressionType() {
            return findExpressionType(signatureParam.getType(), expression);
        }

    }

    static class OneParameterEqualsFactory extends EvaluatorFactory {
        public OneParameterEqualsFactory(IParameterDeclaration signatureParam, String expression) {
            super(signatureParam, expression);
        }

        @Override
        public boolean hasMin() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasMax() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean needsIncrement(Bound bound) {
            throw new UnsupportedOperationException();
        }

    }

    static class OneParameterRangeFactory extends EvaluatorFactory {
        IParameterDeclaration conditionParam;

        public OneParameterRangeFactory(IParameterDeclaration signatureParam,
                IParameterDeclaration conditionParam,
                RelationType relation,
                String expression) {
            super(signatureParam, expression);

            this.conditionParam = conditionParam;
            this.relation = relation;
        }

        RelationType relation;

        @Override
        public boolean hasMin() {
            return !relation.isLessThan();
        }

        @Override
        public boolean hasMax() {
            return relation.isLessThan();
        }

        @Override
        public boolean needsIncrement(Bound bound) {
            return relation.getIncBound() == bound;
        }

    }

    static class TwoParameterRangeFactory extends EvaluatorFactory {
        IParameterDeclaration conditionParam1;
        IParameterDeclaration conditionParam2;
        RelationType relation1, relation2;

        public TwoParameterRangeFactory(IParameterDeclaration signatureParam,
                IParameterDeclaration conditionParam1,
                RelationType relation1,
                IParameterDeclaration conditionParam2,
                RelationType relation2,
                String expression) {
            super(signatureParam, expression);

            this.conditionParam1 = conditionParam1;
            this.relation1 = relation1;
            this.conditionParam2 = conditionParam2;
            this.relation2 = relation2;
        }

        @Override
        public boolean hasMin() {
            return true;
        }

        @Override
        public boolean hasMax() {
            return true;
        }

        @Override
        public boolean needsIncrement(Bound bound) {
            if (bound == Bound.LOWER)
                return relation1 == RelationType.LT;
            return relation2 == RelationType.LE;
        }

    }

    enum RelationType {

        LT("<", ">", true, null),
        LE("<=", ">=", true, Bound.UPPER),
        GE(">=", "<=", false, null),
        GT(">", "<", false, Bound.LOWER);

        private RelationType(String func, String opposite, boolean lessThan, Bound incBound) {
            this.func = func;
            this.opposite = opposite;
            this.lessThan = lessThan;
            this.incBound = incBound;
        }

        public Bound getIncBound() {
            return incBound;
        }

        public boolean isLessThan() {
            return lessThan;
        }

        String func;
        String opposite;

        boolean lessThan;
        Bound incBound;

        static RelationType findElement(String code) {
            RelationType[] all = values();
            for (int i = 0; i < all.length; i++) {
                if (code.equals(all[i].func))
                    return all[i];
            }

            return null;
        }

    };

}
