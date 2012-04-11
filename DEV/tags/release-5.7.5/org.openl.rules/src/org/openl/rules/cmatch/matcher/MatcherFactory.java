package org.openl.rules.cmatch.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.types.IOpenClass;

public class MatcherFactory {
    private static final Map<String, List<IMatcherBuilder>> matcherBuilders = new HashMap<String, List<IMatcherBuilder>>();

    static {
        // = (match)
        registerBuilder(new EnumMatchBuilder());
        registerBuilder(new NumberMatchBuilder());
        registerBuilder(new ClassMatchBuilder());
        registerBuilder(new BooleanPrimitiveMatch());

        // min
        registerBuilder(new NumberMinBuilder());
        registerBuilder(ClassMinMaxBuilder.minBuilder(Date.class));

        // max
        registerBuilder(new NumberMaxBuilder());
        registerBuilder(ClassMinMaxBuilder.maxBuilder(Date.class));
    }

    public static IMatcher getMatcher(String operationName, IOpenClass type) {
        List<IMatcherBuilder> builders = matcherBuilders.get(operationName);
        if (builders == null) {
            // unknown operation
            return null;
        }

        IMatcher result = null;
        for (IMatcherBuilder builder : builders) {
            result = builder.getInstanceIfSupports(type);
            if (result != null) {
                break;
            }
        }

        return result;
    }

    public static void registerBuilder(IMatcherBuilder builder) {
        String operationName = builder.getName();
        List<IMatcherBuilder> builders = matcherBuilders.get(operationName);

        if (builders == null) {
            builders = new ArrayList<IMatcherBuilder>();
            matcherBuilders.put(operationName, builders);
        } else {
            if (builders.contains(builder)) {
                throw new IllegalArgumentException("MatcherBuilder is registered already!");
            }
        }

        builders.add(builder);
    }
}
