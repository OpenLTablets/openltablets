/* Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package org.openl.rules.rest.model.converters;

import java.io.IOException;

import org.openl.rules.project.abstraction.ProjectStatus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom deserializer for {@link ProjectStatus} enum.
 *
 * @author Vladyslav Pikus
 */
public class ProjectStatusDeserializer extends JsonDeserializer<ProjectStatus> {

    @Override
    public ProjectStatus deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String value = parser.getValueAsString();

        if (value == null) {
            return null;
        }

        if ("OPENED".equals(value)) {
            return ProjectStatus.VIEWING;
        }

        return ProjectStatus.valueOf(value);
    }
}
