package org.openl.engine;

import org.openl.IOpenParser;
import org.openl.OpenL;
import org.openl.message.OpenLMessagesUtils;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.SourceType;
import org.openl.syntax.code.IParsedCode;
import org.openl.syntax.code.impl.ParsedCode;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;

/**
 * Class that defines OpenL engine manager implementation for parsing operations.
 * 
 */
public class OpenLParseManager extends OpenLHolder {

    /**
     * Creates new instance of OpenL engine manager.
     * 
     * @param openl {@link OpenL} instance
     */
    public OpenLParseManager(OpenL openl) {
        super(openl);
    }

    /**
     * Parses source.
     * 
     * @param source source
     * @param sourceType type that describes parsing algorithm
     * @return {@link IParsedCode} instance
     */
    public IParsedCode parseSource(IOpenSourceCodeModule source, SourceType sourceType) {

        IOpenParser parser = getParser();

        switch (sourceType) {
            case MODULE:
                return parser.parseAsModule(source);
            case METHOD_HEADER:
                return parser.parseAsMethodHeader(source);
            case METHOD_BODY:
                return parser.parseAsMethodBody(source);
            case TYPE:
                return parser.parseAsType(source);
            case INT_RANGE:
                return parser.parseAsIntegerRange(source);
            case DOUBLE_RANGE:
                return parser.parseAsFloatRange(source);

            default:
                return getInvalidCode(source);
        }
    }

    /**
     * Gets the parser.
     * 
     * @return parser
     */
    private IOpenParser getParser() {

        return getOpenL().getParser();
    }

    /**
     * Gets code that cannot be parsed by parser.
     * 
     * @param source source
     * @return {@link IParsedCode} instance
     */
    private IParsedCode getInvalidCode(IOpenSourceCodeModule source) {

        String message = String.format("Invalid source type: %s", source.getUri(0));
        SyntaxNodeException error = SyntaxNodeExceptionUtils.createError(message, source);

        OpenLMessagesUtils.addError(error);

        return new ParsedCode(null, source, new SyntaxNodeException[] { error });
    }

}
