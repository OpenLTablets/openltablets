package org.open.rules.project.validation.openapi;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.method.ExecutableRulesMethod;
import org.openl.rules.project.model.RulesDeploy;
import org.openl.rules.project.validation.base.ValidatedCompiledOpenClass;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;

class Context {
    private ValidatedCompiledOpenClass validatedCompiledOpenClass;
    private RulesDeploy rulesDeploy;
    private OpenAPI expectedOpenAPI;
    private OpenAPI actualOpenAPI;
    private IOpenClass openClass;
    private Class<?> serviceClass;
    private Map<Method, Method> methodMap;
    private boolean provideRuntimeContext;
    private boolean provideVariations;
    private JXPathContext expectedOpenAPIJXPathContext;
    private JXPathContext actualOpenAPIJXPathContext;

    private String path;
    private String operationType;
    private PathItem expectedPathItem;
    private PathItem actualPathItem;
    private Operation expectedOperation;
    private Operation actualOperation;
    private MediaType expectedMediaType;
    private MediaType actualMediaType;

    private Method method;
    private IOpenMethod openMethod;

    private boolean typeValidationInProgress;
    private IOpenClass type;

    public ValidatedCompiledOpenClass getValidatedCompiledOpenClass() {
        return validatedCompiledOpenClass;
    }

    public void setValidatedCompiledOpenClass(ValidatedCompiledOpenClass validatedCompiledOpenClass) {
        this.validatedCompiledOpenClass = validatedCompiledOpenClass;
    }

    public OpenAPI getExpectedOpenAPI() {
        return expectedOpenAPI;
    }

    public void setExpectedOpenAPI(OpenAPI expectedOpenAPI) {
        this.expectedOpenAPI = expectedOpenAPI;
    }

    public OpenAPI getActualOpenAPI() {
        return actualOpenAPI;
    }

    public void setActualOpenAPI(OpenAPI actualOpenAPI) {
        this.actualOpenAPI = actualOpenAPI;
    }

    public IOpenClass getOpenClass() {
        return openClass;
    }

    public void setOpenClass(IOpenClass openClass) {
        this.openClass = openClass;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public boolean isProvideRuntimeContext() {
        return provideRuntimeContext;
    }

    public void setProvideRuntimeContext(boolean provideRuntimeContext) {
        this.provideRuntimeContext = provideRuntimeContext;
    }

    public boolean isProvideVariations() {
        return provideVariations;
    }

    public void setProvideVariations(boolean provideVariations) {
        this.provideVariations = provideVariations;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PathItem getExpectedPathItem() {
        return expectedPathItem;
    }

    public void setExpectedPathItem(PathItem expectedPathItem) {
        this.expectedPathItem = expectedPathItem;
    }

    public PathItem getActualPathItem() {
        return actualPathItem;
    }

    public void setActualPathItem(PathItem actualPathItem) {
        this.actualPathItem = actualPathItem;
    }

    public Operation getExpectedOperation() {
        return expectedOperation;
    }

    public void setExpectedOperation(Operation expectedOperation) {
        this.expectedOperation = expectedOperation;
    }

    public Operation getActualOperation() {
        return actualOperation;
    }

    public void setActualOperation(Operation actualOperation) {
        this.actualOperation = actualOperation;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public IOpenMethod getOpenMethod() {
        return openMethod;
    }

    public void setOpenMethod(IOpenMethod openMethod) {
        this.openMethod = openMethod;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public MediaType getExpectedMediaType() {
        return expectedMediaType;
    }

    public void setExpectedMediaType(MediaType expectedMediaType) {
        this.expectedMediaType = expectedMediaType;
    }

    public MediaType getActualMediaType() {
        return actualMediaType;
    }

    public void setActualMediaType(MediaType actualMediaType) {
        this.actualMediaType = actualMediaType;
    }

    public JXPathContext getExpectedOpenAPIJXPathContext() {
        return expectedOpenAPIJXPathContext;
    }

    public void setExpectedOpenAPIJXPathContext(JXPathContext expectedOpenAPIJXPathContext) {
        this.expectedOpenAPIJXPathContext = expectedOpenAPIJXPathContext;
    }

    public JXPathContext getActualOpenAPIJXPathContext() {
        return actualOpenAPIJXPathContext;
    }

    public void setActualOpenAPIJXPathContext(JXPathContext actualOpenAPIJXPathContext) {
        this.actualOpenAPIJXPathContext = actualOpenAPIJXPathContext;
    }

    public Map<Method, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<Method, Method> methodMap) {
        this.methodMap = methodMap;
    }

    public RulesDeploy getRulesDeploy() {
        return rulesDeploy;
    }

    public void setRulesDeploy(RulesDeploy rulesDeploy) {
        this.rulesDeploy = rulesDeploy;
    }

    public boolean isTypeValidationInProgress() {
        return typeValidationInProgress;
    }

    public IOpenClass getType() {
        return type;
    }

    public void setType(IOpenClass type) {
        this.type = type;
    }

    public void setTypeValidationInProgress(boolean typeValidationInProgress) {
        this.typeValidationInProgress = typeValidationInProgress;
    }

    public TableSyntaxNode getTableSyntaxNode() {
        if (getOpenMethod() instanceof ExecutableRulesMethod) {
            ExecutableRulesMethod executableRulesMethod = (ExecutableRulesMethod) getOpenMethod();
            return executableRulesMethod.getSyntaxNode();
        }
        return null;
    }

}
