package org.openl.rules.workspace.abstracts;

import java.util.Collection;

/**
 * Defines path of an Artefact.
 * I.e. location of some particular artefact in a hierarchy of artefacts
 * or in a tree of artefacts.
 * 
 * @author Aleh Bykhavets
 */
public interface ArtefactPath {
    /**
     * Gets a segment in the path.
     *
     * @param index position of segment in the path
     * @return value of segment
     */
    public String segment(int index);

    /**
     * Returns number of segments in the path
     *
     * @return integer number of segments
     */
    public int segmentCount();

    /**
     * Gets the path as a collection of segments.
     *
     * @return collection of segments
     */
    public Collection<String> getSegments();

    /**
     * Gets the path as a single string.
     * All segments are concatenated by special delimiter '/'.
     *
     * @return string with the path
     */
    public String getStringValue();

    /**
     * Gets the path as a single string, omitting first <code>skip</code> elements.
     * All segments are concatenated by special delimiter '/'.
     *
     * @param skip number of elements to skip
     * 
     * @return string with the path
     */
    public String getStringValue(int skip);

    /**
     * Create new instance of ArtefactPath from base one,
     * excluding first segment of base path.
     * <p/>
     * It is used to translate path of artefact in the workspace 
     * to path in a project.
     * 
     * @return new instance where first segment of base path is excluded.
     */
    public ArtefactPath withoutFirstSegment();

    /**
     * Creates new instance of ArtefactPath from base one,
     * adding one more segment.
     * <p/>
     * It should be used to build artefact paths recursively.
     * 
     * @param segment adding segment
     * @return new instance where specified segment is appended to path
     */
    public ArtefactPath withSegment(String segment);
}
