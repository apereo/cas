package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * WebApplicationService representing an SAML 2.0 request.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Entity
@DiscriminatorValue("saml")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Slf4j
public class Saml20WebApplicationService extends AbstractWebApplicationService {

    /**
     * Instantiates a new Simple web application service.
     *
     * @param id          the id
     * @param originalUrl the original url
     * @param artifactId  the artifact id
     */
    @JsonCreator
    protected Saml20WebApplicationService(@JsonProperty("id") final String id,
                                          @JsonProperty("originalUrl") final String originalUrl,
                                          @JsonProperty("artifactId") final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}
