package org.apereo.cas.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link CasReferenceProperty}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@EqualsAndHashCode(of = "name")
@ToString
@Setter
public class CasReferenceProperty implements Serializable, Comparable<CasReferenceProperty> {
    private static final long serialVersionUID = 6084780445748297104L;

    private final boolean expressionLanguage;

    private final boolean duration;
    
    private final boolean required;

    private final String module;

    private final String owner;

    private final String type;

    private String description;

    private final String shortDescription;

    private final String name;

    private final Object defaultValue;

    private final String deprecationLevel;

    private final String deprecationReason;

    private final String deprecationReplacement;

    private final String sourceType;

    @Override
    public int compareTo(final CasReferenceProperty o) {
        return this.name.compareTo(o.getName());
    }
}
