package org.apereo.cas.util.feature;

import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

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
@Jacksonized
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class CasRuntimeModule implements Serializable {
    @Serial
    private static final long serialVersionUID = -1581604787854700568L;

    private final String name;

    private final String version;

    private final String description;
}
