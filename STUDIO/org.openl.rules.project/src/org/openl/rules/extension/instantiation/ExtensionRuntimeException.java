package org.openl.rules.extension.instantiation;

public class ExtensionRuntimeException extends RuntimeException {

    public ExtensionRuntimeException() {
        super();
    }

    public ExtensionRuntimeException(String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExtensionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionRuntimeException(String message) {
        super(message);
    }

    public ExtensionRuntimeException(Throwable cause) {
        super(cause);
    }

}
