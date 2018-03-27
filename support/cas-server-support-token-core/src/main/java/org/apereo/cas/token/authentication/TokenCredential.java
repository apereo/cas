package org.apereo.cas.token.authentication;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link TokenCredential} that represents the user credentials in form of an encrypted token.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
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
}
