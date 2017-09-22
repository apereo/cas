package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a service which wishes to use the CAS protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleWebApplicationServiceImpl extends AbstractWebApplicationService {

    private static final long serialVersionUID = 8334068957483758042L;

    /**
     * Instantiates a new Simple web application service.
     *
     * @param id          the id
     * @param originalUrl the original url
     * @param artifactId  the artifact id
     */
    @JsonCreator
    protected SimpleWebApplicationServiceImpl(@JsonProperty("id") final String id,
                                              @JsonProperty("originalUrl") final String originalUrl,
                                              @JsonProperty("artifactId") final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}

