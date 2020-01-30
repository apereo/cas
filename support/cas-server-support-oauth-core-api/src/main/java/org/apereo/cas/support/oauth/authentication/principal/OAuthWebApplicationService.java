package org.apereo.cas.support.oauth.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * WebApplicationService representing an OAuth request.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Entity
@DiscriminatorValue("oauth")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class OAuthWebApplicationService extends AbstractWebApplicationService {

    /**
     * Instantiates a new Simple web application service.
     *
     * @param id          the id
     * @param originalUrl the original url
     * @param artifactId  the artifact id
     */
    @JsonCreator
    protected OAuthWebApplicationService(@JsonProperty("id") final String id,
                                         @JsonProperty("originalUrl") final String originalUrl,
                                         @JsonProperty("artifactId") final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}
