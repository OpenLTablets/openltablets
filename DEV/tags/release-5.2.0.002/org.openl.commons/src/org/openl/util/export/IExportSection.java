package org.openl.util.export;

/**
 * A tree like structure for OpenL custom serialization.
 * Contains data {@link #getRows()} and child nodes {@link #getSubSections()}.
 */
public interface IExportSection<T> {
    /**
     * Returns section name.
     *
     * @return name
     */
    String getName();

    /**
     * Returns the java class this section represents.
     *
     * @return a class
     */
    Class<T> getExportedClass();

    /**
     * Returns array of subsections of this section. Can be <code>null</code>.
     *
     * @return child sections
     */
    IExportSection<T>[] getSubSections();

    /**
     * Return array of rows - section data. Can be <code>null</code>.
     * @return section rows.
     */
    IExportRow[] getRows();
}
