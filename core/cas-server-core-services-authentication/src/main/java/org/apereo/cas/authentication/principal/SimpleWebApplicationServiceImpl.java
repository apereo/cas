package org.apereo.cas.authentication.principal;

import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Represents a service which wishes to use the CAS protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Entity
@DiscriminatorValue("simple")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SimpleWebApplicationServiceImpl extends AbstractWebApplicationService {

    @Serial
    private static final long serialVersionUID = 8334068957483758042L;

    @JsonCreator
    protected SimpleWebApplicationServiceImpl(@Nullable @JsonProperty("id") final String id,
                                              @JsonProperty("originalUrl") final String originalUrl,
                                              @Nullable @JsonProperty("artifactId") final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}
