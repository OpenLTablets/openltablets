package org.openl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class StringTool {

    public static abstract class CharToStringConverter {
        public String convert(char ch) {
            String res = convertOrNull(ch);
            if (res == null) {
                return String.valueOf(ch);
            }
            return res;
        }

        public abstract String convertOrNull(char ch);
    }

    static public interface Convertor {
        public void convert(char c, int idx, StringBuffer out);
    }

    static public interface MacroKeyHandler {
        public void handleKey(String key, MacroSubst ms, StringBuffer out);
    }

    static public class MacroSubst extends TextTransformer {

        public char _macroDelim;
        public Map<String, String> _macros;
        MacroKeyHandler _mkh;

        public MacroSubst(Map<String, String> macros, char macroDelim, MacroKeyHandler mkh) {
            _macros = macros;
            _macroDelim = macroDelim;
            _mkh = mkh;
        }

        /**
         * cur == delim delim != delim !=delim status == 1 0 0 1
         * ====================================================== flush out tmp
         * status=0 status=1
         */

        @Override
        public void process() {
            if (cur == _macroDelim) {
                if (status == 0) {
                    status = 1;
                } else {
                    status = 0;
                    String key = flushTmp();
                    String res = (String) _macros.get(key);
                    if (res == null) {
                        _mkh.handleKey(key, this, out);
                    } else {
                        out.append(res);
                    }
                }
            } else if (status == 0) {
                out.append(cur);
            } else {
                tmp().append(cur);
            }
        }

        // protected void handleUnknownKey(String key)
        // {
        // switch(_mode)
        // {
        // case COPY:
        // out.append(_macroDelim).append(key).append(_macroDelim);
        // break;
        // case ERROR:
        // throw new RuntimeException("Macro key: " + key + " is not found");
        // case EMPTY:
        // }
        // do nothing
        // throw new RuntimeException("Key " + key + " is not found");
        // }
    }

    static public interface Selector {
        public boolean select(char c, int idx);
    }

    static public class TextTransformer {
        static final public char EOF = (char) -1;
        protected char prev = EOF, cur = EOF, next = EOF;
        protected StringBuffer out = null;
        private StringBuffer tmp = null;
        protected int len = -1;
        protected int status = 0;
        protected int idx = 0;

        public String flushTmp() {
            String s = tmp().toString();
            tmp.setLength(0);
            return s;
        }

        public void process() {
            out.append(cur);
        }

        public final StringBuffer tmp() {
            if (tmp == null) {
                tmp = new StringBuffer();
            }
            return tmp;
        }

        public String transform(String src) {
            return transform(src, new StringBuffer()).toString();
        }

        public StringBuffer transform(String src, StringBuffer buf) {
            out = buf;
            len = src.length();
            if (len > 0) {
                next = src.charAt(0);
            }

            for (idx = 0; idx < len; idx++) {
                prev = cur;
                cur = next;
                next = idx + 1 < len ? src.charAt(idx + 1) : EOF;

                process();
            }

            veryEnd();

            return out;

        }

        public void veryEnd() {
        }
    }

    static final char[] STRING_TO_XML_REPLACE_FROM = { '>', '<', '&', '\'', '"' };

    static final String[] STRING_TO_XML_REPLACE_TO = { "&gt;", "&lt;", "&amp;", "&#39;", "&quot;" };

    static final char[] STRING_TO_XML_BODY_REPLACE_FROM = { '>', '<', '&', '\'', '"', ' ' };

    static final String[] STRING_TO_XML_BODY_REPLACE_TO = { "&gt;", "&lt;", "&amp;", "&#39;", "&quot;", "&nbsp;" };

    static final public int EMPTY = 1, COPY = 2, ERROR = 3;

    public static Selector VALID_JAVA_IDENTIFICATOR = new Selector() {
        public boolean select(char c, int idx) {
            if (idx > 0) {
                return Character.isJavaIdentifierPart(c);
            } else {
                return Character.isJavaIdentifierStart(c);
            }
        }
    };

    static public Convertor IGNORE = new Convertor() {
        public void convert(char c, int idx, StringBuffer out) {
        }
    };

    static public Convertor UPPER = new Convertor() {
        public void convert(char c, int idx, StringBuffer out) {
            out.append(Character.toUpperCase(c));
        }
    };

    static public Convertor LOWER = new Convertor() {
        public void convert(char c, int idx, StringBuffer out) {
            out.append(Character.toLowerCase(c));
        }
    };

    static public final MacroKeyHandler MKH_DONOTHING = new MacroKeyHandler() {
        public void handleKey(String key, MacroSubst ms, StringBuffer out) {
        }
    };

    static public final MacroKeyHandler MKH_LEAVE = new MacroKeyHandler() {
        public void handleKey(String key, MacroSubst ms, StringBuffer out) {
            out.append(ms._macroDelim).append(key).append(ms._macroDelim);
        }
    };

    static public final MacroKeyHandler MKH_ERROR = new MacroKeyHandler() {
        public void handleKey(String key, MacroSubst ms, StringBuffer out) {
            throw new RuntimeException("Key " + key + " not found");
        }
    };

    public static StringBuffer append(StringBuffer buf, char c, int n) {
        for (int i = 0; i < n; i++) {
            buf.append(c);
        }
        return buf;
    }

    /**
     * Create hexadecimal string representation of a specified number of bytes
     * from array (padded with 0s)
     *
     * @param src source byte array
     * @param off offset
     * @param len length
     * @return hex string
     */
    public static String byteArrayToHexString(byte[] src, int off, int len) {
        StringBuffer out = new StringBuffer();
        for (int i = off; i < off + len; i++) {
            String s = Integer.toHexString(src[i] & 0xFF);
            if (s.length() % 2 != 0) {
                out.append("0");
            }
            out.append(s);
        }
        return out.toString();
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return Character.isUpperCase(s.charAt(0)) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * See examples below: 
     * 1) Assert.assertEquals("url", StringTool.decapitalizeName("URL", "_")); 
     * 2) Assert.assertEquals("driver", StringTool.decapitalizeName("Driver", "_"));
     * 3) Assert.assertEquals("test_url", StringTool.decapitalizeName("TestURL", "_")); 
     * 4) Assert.assertEquals("testurl", StringTool.decapitalizeName("testURL", null));
     * 5) Assert.assertEquals("test_url_code", StringTool.decapitalizeName("TestURLCode", "_"));
     * 6) Assert.assertEquals("url_code", StringTool.decapitalizeName("URLCode", "_"));
     */

    public static String decapitalizeName(String capitalized, String separator) {
        return decapitalizeName(capitalized, separator, new StringBuffer()).toString();
    }

    public static StringBuffer decapitalizeName(String capitalized, String separator, StringBuffer buf) {
        if (capitalized == null) {
            return buf;
        }
        if (separator == null) {
            separator = "";
        }

        // StringBuffer buf = new StringBuffer();
        int start = 0;
        boolean prevUP = false;

        char[] src = capitalized.toCharArray();

        for (int i = 0; i < src.length; i++) {
            char c = src[i];

            if (Character.isUpperCase(c)) {
                if (!prevUP) {
                    prevUP = true;
                    if (i > start) {
                        buf.append(src, start, i - start);
                        start = i;
                        buf.append(separator);
                    }
                }
                src[i] = Character.toLowerCase(c);
            } else // lower case
            {
                if (prevUP) {
                    prevUP = false;
                    int len = i - start;

                    if (len > 1) {
                        buf.append(src, start, len - 1);
                        start = i - 1;
                        buf.append(separator);
                    }
                }
            } // else
        } // for

        buf.append(src, start, src.length - start);
        return buf;
    }
    public static String encodeHTMLBody(String content) {
        StringBuffer buf = new StringBuffer(100);
        encodeHTMLBody(content, buf);
        return buf.toString();
    }

    public static StringBuffer encodeHTMLBody(String content, StringBuffer buf) {
        prepareXMLAttributeValue(content, buf);
        return buf;
    }

    /**
     * @param string
     * @return
     */
    public static String encodeJavaScriptString(String src) {
        return encodeJavaScriptString(src, new StringBuffer(src.length() + 100)).toString();
    }
    public static StringBuffer encodeJavaScriptString(String src, StringBuffer buf) {
        int len = src.length();
        for (int i = 0; i < len; i++) {
            int ch = src.charAt(i);
            switch (ch) {
                case '\\':
                    buf.append("\\\\");
                    break;
                case '\'':
                    buf.append("\\\'");
                    break;
                case '\"':
                    buf.append("\\\"");
                    break;
                case '&':
                    buf.append("\\&");
                    break;
                case '\t':
                    buf.append("\\t");
                    break;
                case '\b':
                    buf.append("\\b");
                    break;
                case '\n':
                    buf.append("\\n");
                    break;
                case '\r':
                    buf.append("\\r");
                    break;
                case '\f':
                    buf.append("\\f");
                    break;
                default:
                    buf.append((char) ch);
            }
        }
        return buf;

    }

    public static String encodeURL(String url) {
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedUrl;
    }
    
    public static String decodeURL(String url) {
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedUrl;
    }

    /**
     * Transforms String using the following rule: if c is not selected,
     * convertor is called to transform it, otherwise c is put into output
     */
    public static String filter(String src, Selector sel, Convertor conv) {
        return filter(src, sel, conv, new StringBuffer()).toString();
    }

    /**
     * Transforms String using the following rule: if c is not selected,
     * convertor is called to transform it, otherwise c is put into output
     */
    public static StringBuffer filter(String src, Selector sel, Convertor conv, StringBuffer buf) {
        int len = src.length();

        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if (!sel.select(c, i)) {
                conv.convert(c, i, buf);
            } else {
                buf.append(c);
            }
        }

        return buf;
    }

    public static String firstToken(String src, String delim) {
        String[] tokens = tokenize(src, delim);
        return tokens.length > 0 ? tokens[0] : "";
    }

    public static int indexOfClosingBracket(String src, char openingBracket, char closingBracket, int fromIndex) {
        int len = src.length();
        int cnt = 1;
        for (int i = fromIndex; i < len; i++) {
            char c = src.charAt(i);

            if (c == closingBracket) {
                if (--cnt == 0) {
                    return i;
                }
            } else if (c == openingBracket) {
                ++cnt;
            }
        }
        return -1;
    }

    static public boolean isSpace(char c) {
        return c <= ' ' || Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    public static String keepChars(String src, String toKeep) {
        StringBuffer buf = new StringBuffer(src.length());
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (toKeep.indexOf(c) >= 0) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String lastToken(String src, String delim) {
        String[] tokens = tokenize(src, delim);
        return tokens.length > 0 ? tokens[tokens.length - 1] : "";
    }

    public static String macroSubst(String src, Map<String, String> macros, char macroDelim, MacroKeyHandler mkh) {
        return macroSubst(src, macros, macroDelim, mkh, new StringBuffer()).toString();
    }

    public static StringBuffer macroSubst(String src, Map<String, String> macros, char macroDelim, MacroKeyHandler mkh, StringBuffer buf) {
        MacroSubst ms = new MacroSubst(macros, macroDelim, mkh);
        return ms.transform(src, buf);
    }

    public static String makeJavaIdentifier(String src) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (i == 0) {
                buf.append(Character.isJavaIdentifierStart(c) ? c : '_');
            } else {
                buf.append(Character.isJavaIdentifierPart(c) ? c : '_');
            }
        }

        return buf.toString();
    }

    public static String[] openBrackets(String src, char openingBracket, char closingBracket, String ignore) {
        int len = src.length();
        List<String> v = new ArrayList<String>();

        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);

            if (c == openingBracket) {
                int closed = indexOfClosingBracket(src, openingBracket, closingBracket, i + 1);
                if (closed == -1) {
                    throw new RuntimeException("Expected: " + closingBracket);
                }

                v.add(src.substring(i + 1, closed));
                i = closed;
                continue;
            }

            if (ignore.indexOf(c) == -1) {
                throw new RuntimeException("UnExpected: " + c);
            }

        }

        return (String[]) v.toArray(new String[v.size()]);
    }

    public static StringBuffer prepareXMLAttributeValue(String attrValue, StringBuffer buf) {
        return replaceCharsWithStrings(buf, attrValue, STRING_TO_XML_REPLACE_FROM, STRING_TO_XML_REPLACE_TO);
    }

    public static String prepareXMLBodyValue(String content) {
        StringBuffer buf = new StringBuffer(100);
        prepareXMLBodyValue(content, buf);
        return buf.toString();
    }

    public static StringBuffer prepareXMLBodyValue(String attrValue, StringBuffer buf) {
        return replaceCharsWithStrings(buf, attrValue, STRING_TO_XML_BODY_REPLACE_FROM, STRING_TO_XML_BODY_REPLACE_TO);
    }

    public static String propetyToLabel(String propertyName) {
        return propetyToLabel(propertyName, new StringBuffer()).toString();
    }

    public static StringBuffer propetyToLabel(String propertyName, StringBuffer sb) {
        if (propertyName == null || propertyName.length() == 0) {
            return sb;
        // StringBuffer sb = new StringBuffer();
        }

        int len = propertyName.length();
        int i = 0;
        char lastChar = ' ';
        while (i < len) {
            char c = propertyName.charAt(i);
            if (c == '_') {
                if (sb.length() > 0) {
                    lastChar = ' ';
                    sb.append(lastChar);
                }
                while (++i < len && propertyName.charAt(i) == '_') {
                    ;
                }

                continue;
            }

            if (Character.isUpperCase(c) && (!Character.isUpperCase(lastChar)) && (lastChar != ' ')) {
                lastChar = ' ';
                sb.append(lastChar);
            }

            if (lastChar == ' ') {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(c);
            }

            lastChar = c;
            ++i;
        }
        return sb;
    }

    public static String removeChars(String src, String toRemove) {
        StringBuffer buf = new StringBuffer(src.length());
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (toRemove.indexOf(c) < 0) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    // ########################### Helper classes
    // ##################################

    // static public String transform(String src, Selector ignore, Convertor
    // append)
    // {
    //
    // }
    //

    public static String replace(String src, String toFind, String toReplace) {
        return replace(src, toFind, toReplace, true, false, new StringBuffer()).toString();
    }

    public static String replace(String src, String toFind, String toReplace, boolean all, boolean ignoreCase) {
        return replace(src, toFind, toReplace, all, ignoreCase, new StringBuffer()).toString();
    }

    public static StringBuffer replace(String src, String toFind, String toReplace, boolean all, boolean ignoreCase,
            StringBuffer out) {
        int find_len = toFind.length();
        int src_len = src.length();
        int replace_len = toReplace.length();
        int start = 0;

        String test_src = src;
        String test_tofind = toFind;
        if (ignoreCase) {
            test_src = src.toLowerCase();
            test_tofind = toFind.toLowerCase();
        }

        while (start + find_len <= src_len) {
            int idx = test_src.indexOf(test_tofind, start);
            if (idx < 0) {
                break;
            }

            if (start != idx) {
                out.append(src.substring(start, idx));
            }
            if (replace_len > 0) {
                out.append(toReplace);
            }
            start = idx + find_len;
            if (!all) {
                break;
            }
        }

        if (start < src_len) {
            out.append(src.substring(start));
        }
        return out;
    }

    public static final String replaceCharsWithStrings(String sourceStr, char[] replaceFrom, String[] replaceTo) {
        if (sourceStr == null || sourceStr.equals("")) {
            return "";
        }
        if (replaceFrom.length == 0) {
            return sourceStr;
        }
        int length = sourceStr.length();
        StringBuffer result = new StringBuffer(length + 20);
        replaceCharsWithStrings(result, sourceStr, replaceFrom, replaceTo);
        return result.toString();
    }

    public static final String replaceCharsWithStrings(String sourceStr, CharToStringConverter converter) {
        if (sourceStr == null || sourceStr.equals("")) {
            return "";
        }
        if (converter == null) {
            return sourceStr;
        }
        int length = sourceStr.length();
        StringBuffer result = new StringBuffer(length + 20);
        replaceCharsWithStrings(result, sourceStr, converter);
        return result.toString();
    }

    public static final StringBuffer replaceCharsWithStrings(StringBuffer result, String sourceStr, char[] replaceFrom,
            String[] replaceTo) {
        if (replaceFrom.length != replaceTo.length) {
            throw new RuntimeException("Program Error: replaceFrom.length != replaceTo.length");
        }
        if (sourceStr == null || sourceStr.equals("")) {
            return result;
        }
        if (replaceFrom.length == 0) {
            result.append(sourceStr);
            return result;
        }
        int length = sourceStr.length();
        for (int i = 0; i < length; i++) {
            char nextChar = sourceStr.charAt(i);
            boolean isFound = false;
            for (int j = 0; j < replaceFrom.length; j++) {
                char replaceChar = replaceFrom[j];
                if (nextChar == replaceChar) {
                    result.append(replaceTo[j]);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                result.append(nextChar);
            }
        }

        return result;
    }

    public static final void replaceCharsWithStrings(StringBuffer result, String sourceStr,
            CharToStringConverter converter) {
        if (sourceStr == null || sourceStr.equals("")) {
            return;
        }
        if (converter == null) {
            result.append(sourceStr);
            return;
        }
        int length = sourceStr.length();
        for (int i = 0; i < length; i++) {
            char nextChar = sourceStr.charAt(i);
            String newChar = converter.convertOrNull(nextChar);
            if (newChar == null) {
                result.append(nextChar);
            } else {
                result.append(newChar);
            }
        }
    };

    public static String[] splitLines(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        List<String> v = new ArrayList<String>();
        String s;

        try {
            try {
                while ((s = br.readLine()) != null) {
                    v.add(s);
                }
                return (String[]) v.toArray(new String[v.size()]);
            } finally {
                br.close();
            }
        } catch (IOException e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }

    }

    public static String[] splitLines(String src) {
        return splitLines(new StringReader(src));
    }

    public static String[] tokenize(String src, String delim) {
        StringTokenizer st = new StringTokenizer(src, delim);
        int cnt = st.countTokens();
        String[] res = new String[cnt];
        for (int i = 0; i < res.length; i++) {
            res[i] = st.nextToken();
        }
        return res;
    }

    public static String[][] tokenize2(String src, String delim1, String delim2) {
        StringTokenizer st = new StringTokenizer(src, delim1);
        int cnt = st.countTokens();
        String[][] res = new String[cnt][];

        for (int i = 0; i < res.length; i++) {
            res[i] = tokenize(st.nextToken(), delim2);
        }
        return res;
    }

    public static String toValidJavaIdentificator(String src) {
        return filter(src, VALID_JAVA_IDENTIFICATOR, IGNORE);
    }

    public static String trimGood(String src) {
        return trimGood(src, new StringBuffer()).toString();
    }

    public static StringBuffer trimGood(String src, StringBuffer out) {
        int len = src.length();
        int count = len;
        int st = 0;
        // char[] val = src.toCharArray(); /* avoid getfield opcode */

        while ((st < len) && (isSpace(src.charAt(st)))) {
            st++;
        }
        while ((st < len) && (isSpace(src.charAt(len - 1)))) {
            len--;
        }

        if (st > 0 || len < count) {
            if (st != len) {
                out.append(src.substring(st, len));
            }
        } else {
            out.append(src);
        }

        return out;
    }

    static public String untab(String src, int tabSize) {
        StringBuffer buf = new StringBuffer(src.length() + 10);

        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c != '\t') {
                buf.append(c);
            } else {
                buf.append(' ');

                int extra = buf.length() % tabSize;
                if (extra != 0) {
                    append(buf, ' ', tabSize - extra);
                }
            }
        }
        return buf.toString();
    }

    public static String xmlProperty(String key, String value) {
        return xmlProperty(key, value, new StringBuffer()).toString();
    }

    public static StringBuffer xmlProperty(String key, String value, StringBuffer buf) {
        buf.append(key).append("='");
        prepareXMLAttributeValue(value, buf);
        buf.append('\'');
        return buf;
    }

    static public String getFileNameOfJavaClass(Class<?> c)
    {
        return c.getName().replace('.', '/') +  ".java";
    }
    
    /**
     * Split the string by the symbolToSplit. To avoid this symbolToEscape is used.
     * @param src source to process. Can`t be <code>null</code>.
     * @param symbolToSplit the delimiting symbol. Can`t be <code>null</code>.
     * @param symbolToEscape the escaper, that is used to break splitting by symbolToSplit. If <code>null</code>, 
     * the symbolToSplit array will be returned.
     * @return the array of strings computed by splitting this string around matches of the given symbolToSplit and 
     * escaped by escaper.
     */
    public static String[] splitAndEscape(String src, String symbolToSplit, String symbolToEscape) {
        String[] result = null;
        String[] tokens = src.split(symbolToSplit);
        List<String> resultList = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        if (symbolToEscape != null) {
            for (int i=0; i<tokens.length; i++) {
                if (tokens[i].endsWith(symbolToEscape)) {
                    String tokenWithoutEscaper = tokens[i].substring(0,tokens[i].length()-1);
                    buf.append(tokenWithoutEscaper).append(symbolToSplit);                
                } else {
                    if (buf.length() == 0) {
                        resultList.add(tokens[i]);
                    } else {
                        buf.append(tokens[i]);
                        resultList.add(buf.toString());
                        buf.delete(0, buf.length());
                    }                 
                }
            }
            result = (String[]) resultList.toArray(new String[0]); 
        } else {
            result = tokens;
        }
        
        return result; 
    }
    
}