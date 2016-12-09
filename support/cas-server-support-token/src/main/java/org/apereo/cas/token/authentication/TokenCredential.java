package org.apereo.cas.token.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link TokenCredential} that represents the user credentials in form of an encrypted token.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenCredential extends BasicIdentifiableCredential {

    private static final long serialVersionUID = 2749515041385101770L;

    
    private Service service;

    /**
     * Instantiates a new Token credential.
     * @param tokenId    the token
     * @param service  the service
     */
    public TokenCredential(final String tokenId, final Service service) {
        super(tokenId);
        this.service = service;
    }

    public Service getService() {
        return this.service;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("service", this.service)
                .toString();
    }
}
