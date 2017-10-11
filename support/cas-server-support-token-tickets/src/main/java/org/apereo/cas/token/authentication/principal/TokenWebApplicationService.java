package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link TokenWebApplicationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@DiscriminatorValue("token")
public class TokenWebApplicationService extends AbstractWebApplicationService {
    private static final long serialVersionUID = -8844121291312069964L;

    private TokenWebApplicationService() {}
    
    public TokenWebApplicationService(final String id, final String originalUrl, final String artifactId) {
        super(id, originalUrl, artifactId);
    }
}
