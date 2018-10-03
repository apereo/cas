package org.apereo.cas.token.authentication;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link TokenCredential} that represents the user credentials in form of an encrypted token.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@ToString(callSuper = true)
@Getter
@Setter
public class TokenCredential extends BasicIdentifiableCredential {

    private static final long serialVersionUID = 2749515041385101770L;

    private Service service;

    /**
     * Instantiates a new Token credential.
     *
     * @param tokenId the token
     * @param service the service
     */
    public TokenCredential(final String tokenId, final Service service) {
        super(tokenId);
        this.service = service;
    }
}
