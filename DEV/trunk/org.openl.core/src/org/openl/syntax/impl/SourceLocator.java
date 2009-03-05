/*
 * Created on Sep 5, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.syntax.impl;

import org.openl.syntax.ISyntaxError;
import org.openl.syntax.ISyntaxNode;
import org.openl.util.text.AbsolutePosition;
import org.openl.util.text.ILocation;
import org.openl.util.text.TextInfo;
import org.openl.util.text.TextInterval;

/**
 * @author sam
 * 
 */
public class SourceLocator {

    protected TextInfo textInfo;

    /**
     * Constructor from TextInfo.
     */
    public SourceLocator(TextInfo textInfo) {
        this.textInfo = textInfo;
    }

    /**
     * Returns bounding box for a given syntax error.
     */
    public TextInterval getSourceLocation(ISyntaxError error) {
        // TODO replace int[] with TextInterval.expand()
        ILocation loc = error.getLocation();

        // if (Debug.DEBUG)
        // {
        // Debug.debug("TI: " + loc);
        // }

        int[] bbox = loc == null ? null : new int[] { loc.getStart().getAbsolutePosition(textInfo),
                loc.getEnd().getAbsolutePosition(textInfo) };

        bbox = calcBbox(error.getSyntaxNode(), bbox);

        return bbox != null ? new TextInterval(new AbsolutePosition(bbox[0]), new AbsolutePosition(bbox[1])) : null;
    }

    /**
     * Returns bounding box for a given syntax node.
     */
    public TextInterval getSourceLocation(ISyntaxNode node) {
        int[] bbox = calcBbox(node, null);

        return bbox != null ? new TextInterval(new AbsolutePosition(bbox[0]), new AbsolutePosition(bbox[1])) : null;
    }

    /**
     * Returns bounding box for a given syntax node with initial bbox.
     */
    protected int[] calcBbox(ISyntaxNode node, int[] bbox) {
        if (node == null)
            return bbox;

        ILocation loc = node.getSourceLocation();

        // if (Debug.DEBUG)
        // {
        // Debug.debug(node.getType() + " TI: " + loc);
        // }

        if (loc != null) {
            int start = loc.getStart().getAbsolutePosition(textInfo);
            int end = loc.getEnd().getAbsolutePosition(textInfo);
            if (bbox == null) {
                bbox = new int[] { start, end };
            } else {
                bbox[0] = Math.min(bbox[0], start);
                bbox[1] = Math.max(bbox[1], end);
            }
        }

        int N = node.getNumberOfChildren();
        for (int i = 0; i < N; i++) {
            bbox = calcBbox(node.getChild(i), bbox);
        }

        return bbox;
    }

    /**
     * @return
     */
    public TextInfo getTextInfo() {
        return textInfo;
    }

}
