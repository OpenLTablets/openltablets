package org.openl.rules.project.abstraction;

/**
 * Created by AAstrouski on 05.12.13.
 */
public enum ProjectStatus {

    LOCAL ("Local"),
    ARCHIVED ("Archived"),
    VIEWING ("Viewing"),
    VIEWING_VERSION ("Viewing Version"),
    EDITING ("In Editing"),
    CLOSED ("Closed");

    private String displayValue;

    ProjectStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

}
