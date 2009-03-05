package org.openl.rules.ui.tablewizard;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.openl.rules.domaintree.DomainTree;
import org.openl.rules.domaintree.DomainTreeContext;
import org.apache.commons.lang.StringUtils;

/**
 * @author Aliaksandr Antonik.
 */
public class DomainTreePath {
    private String dotExpression;
    private DomainTree domainTree;
    private DomainTreeContext context;
    private Collection<String> rootObjects;

    public Collection<String> getSubExpressions() {
        String typename = domainTree.getTypename(context, dotExpression);
        if (typename == null) { // invalid expression
            return rootObjects;
        }

        return domainTree.getClassProperties(typename);
    }

    private boolean checkDotExpression(String dotExpression) {
        String typename = domainTree.getTypename(context, dotExpression);
        return typename != null;
    }

    public String getDotExpression() {
        return dotExpression;
    }

    public void setDotExpression(String dotExpression) {
        this.dotExpression = dotExpression;
    }

    public void setDomainTree(DomainTree domainTree) {
        this.domainTree = domainTree;
    }

    public void setParameters(List<TypeNamePair> parameters) {
        context = new DomainTreeContext();
        rootObjects = new ArrayList<String>(parameters.size());
        for (TypeNamePair pair : parameters) {
            context.setObjectType(pair.getName(), pair.getType());
            rootObjects.add(pair.getName());
        }
    }

    public String getNewDotPart() {
        return StringUtils.EMPTY;
    }

    public void setNewDotPart(String newDotPart) {
        if (!StringUtils.isBlank(newDotPart)) {
            if (StringUtils.isBlank(dotExpression))
                dotExpression = newDotPart;
            else
                dotExpression += "." + newDotPart;
        }

        if (!checkDotExpression(dotExpression)) {
            dotExpression = StringUtils.EMPTY;
        }
    }
}
