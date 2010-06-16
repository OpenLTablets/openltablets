/**
 * 
 */
package org.openl.rules.tableeditor.model.ui.util;

import org.openl.rules.table.word.WordUrlParser;
import org.openl.rules.table.xls.XlsUrlParser;
import org.openl.util.RuntimeExceptionWrapper;

/**
 * @author Andrei Astrouski
 */
public class HTMLHelper {

    public static String htmlStringWithSelections(String src, String[] tokens) {
        StringHighlighter sf = new StringHighlighter(tokens, src);        
        return sf.highlightStringsInText();
    }

    public static String makeXlsOrDocUrl(String url) {
        if (url == null) {
            return "#";
        }

        XlsUrlParser parser = new XlsUrlParser();
        try {
            parser.parse(url);
        } catch (Exception e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }

        if (parser.isExcelFile()) {
            String ret = "" + "&wbPath=" + parser.wbPath + "&wbName=" + parser.wbName + "&wsName=" + parser.wsName
                    + "&range=" + parser.range;

            return ret;
        }

        WordUrlParser wdparser = new WordUrlParser();
        try {
            wdparser.parse(url);
        } catch (Exception e) {
            throw RuntimeExceptionWrapper.wrap(e);
        }

        String ret = "" + "&wdPath=" + wdparser.wdPath + "&wdName=" + wdparser.wdName + "&wdParStart="
                + wdparser.wdParStart + "&wdParEnd=" + wdparser.wdParEnd;

        return ret;
    }

    private static String toHex(short x) {
        String s = Integer.toHexString(x);

        switch (s.length()) {
            case 1:
                return "0" + s;
            case 2:
                return s;
        }
        return s.substring(s.length() - 2);
    }

    public static String toHexColor(short[] x) {
        if (x == null) {
            return "#000000";
        }
        return "#" + toHex(x[0]) + toHex(x[1]) + toHex(x[2]);
    }

    public static String toRgbColor(short[] color) {
        return "rgb(" + String.valueOf(color[0]) + "," + String.valueOf(color[1]) + "," + String.valueOf(color[2])
                + ")";
    }

    public static String urlLink(String url, String title, String htmltext, String target) {
        String s1 = "<a href=\"" + url + "\"";
        if (title != null) {
            s1 += (" title=\"" + title + "\"");
        }
        if (target != null) {
            s1 += (" target=\"" + target + "\"");
        }
        s1 += (">" + htmltext + "</a>");
        return s1;
    }

}
