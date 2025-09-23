package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.io.Serial;

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
    protected SimpleWebApplicationServiceImpl(@JsonProperty("id") final String id,
                                              @JsonProperty("originalUrl") final String originalUrl,
                                              @JsonProperty("artifactId") final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}
