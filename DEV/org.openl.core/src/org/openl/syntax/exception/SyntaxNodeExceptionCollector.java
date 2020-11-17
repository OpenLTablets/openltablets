package org.openl.syntax.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openl.util.StringUtils;

public class SyntaxNodeExceptionCollector {
    private final List<SyntaxNodeException> syntaxNodeExceptions = new ArrayList<>();

    public void run(Runnable r) throws Exception {
        try {
            r.run();
        } catch (SyntaxNodeException e) {
            syntaxNodeExceptions.add(e);
        } catch (CompositeSyntaxNodeException e) {
            if (e.getErrors() != null) {
                Collections.addAll(syntaxNodeExceptions, e.getErrors());
            }
        }
    }

    public void addSyntaxNodeException(SyntaxNodeException e) {
        for (SyntaxNodeException sne : syntaxNodeExceptions) {
            if (Objects.equals(sne.getMessage(), e.getMessage()) && sne.getSourceUri() != null && Objects
                .equals(sne.getSourceUri(), e.getSourceUri())) {
                return;
            }
        }
        syntaxNodeExceptions.add(e);
    }

    public void throwIfAny() throws SyntaxNodeException {
        throwIfAny(StringUtils.EMPTY);
    }

    public boolean hasErrors() {
        return !syntaxNodeExceptions.isEmpty();
    }

    public void throwIfAny(String msg) throws SyntaxNodeException {
        if (!syntaxNodeExceptions.isEmpty()) {
            if (syntaxNodeExceptions.size() == 1) {
                throw syntaxNodeExceptions.get(0);
            } else {
                throw new CompositeSyntaxNodeException(msg, syntaxNodeExceptions.toArray(new SyntaxNodeException[] {}));
            }
        }
    }
}
