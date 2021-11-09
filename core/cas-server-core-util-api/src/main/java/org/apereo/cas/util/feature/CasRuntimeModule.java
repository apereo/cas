package org.apereo.cas.util.feature;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link CasRuntimeModule}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@EqualsAndHashCode(of = {"name", "version"})
@ToString
@Getter
public class CasRuntimeModule implements Serializable {
    private static final long serialVersionUID = -1581604787854700568L;

    private final String name;

    private final String version;

    private final String description;
}
