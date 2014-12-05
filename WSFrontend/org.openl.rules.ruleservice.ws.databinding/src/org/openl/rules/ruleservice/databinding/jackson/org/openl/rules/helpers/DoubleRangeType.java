package org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.helpers;

import org.openl.rules.helpers.DoubleRange;
import org.openl.util.RangeWithBounds.BoundType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * #%L
 * OpenL - RuleService - RuleService - Web Services Databinding
 * %%
 * Copyright (C) 2013 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

/**
 * Custom mapping for {@link DoubleRange} due to it is not usual bean all
 * results should be registered using the special methods.
 * 
 * @author Marat Kamalov
 */
public class DoubleRangeType {
    @JsonCreator
    public DoubleRangeType(@JsonProperty("lowerBound") double lowerBound,
            @JsonProperty("upperBound") double upperBound,
            @JsonProperty("lowerBoundType") BoundType lowerBoundType,
            @JsonProperty("upperBoundType") BoundType upperBoundType) {
    }
}
