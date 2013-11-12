package org.openl.rules.ui.search;

@Deprecated
public interface IStringFilter {

    class ArrayFilter implements IStringFilter {

        String[] matches;

        public ArrayFilter(String[] matches) {
            this.matches = matches;
        }

        public boolean matchString(String src) {
            for (int i = 0; i < matches.length; i++) {
                if (src.indexOf(matches[i]) >= 0) {
                    return true;
                }
            }
            return false;
        }

    }

    boolean matchString(String src);
}
