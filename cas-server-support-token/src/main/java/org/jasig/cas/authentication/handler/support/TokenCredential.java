package org.jasig.cas.authentication.handler.support;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.BasicIdentifiableCredential;
import org.jasig.cas.authentication.principal.Service;

import javax.validation.constraints.NotNull;

/**
 * This is {@link TokenCredential} that represents the user credentials in form of an encrypted token.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TokenCredential extends BasicIdentifiableCredential {

    private static final long serialVersionUID = 2749515041385101770L;

    @NotNull
    private final Service service;

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
                .append("service", service)
                .toString();
    }
}
