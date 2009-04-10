package org.openl.rules.cmatch.matcher;

import org.openl.rules.data.IString2DataConvertor;
import org.openl.rules.data.String2DataConvertorFactory;
import org.openl.types.IOpenClass;

public class ClassMatchBuilder implements IMatcherBuilder {

    public IMatcher getInstanceIfSupports(IOpenClass type) {
        Class<?> c = type.getInstanceClass();
        if (Comparable.class.isAssignableFrom(c)) {
            IString2DataConvertor convertor = String2DataConvertorFactory.getConvertor(c);
            return new ClassMatchMatcher(c, convertor);
        } else {
            return null;
        }
    }

    public String getName() {
        return OP_MATCH;
    }

}
