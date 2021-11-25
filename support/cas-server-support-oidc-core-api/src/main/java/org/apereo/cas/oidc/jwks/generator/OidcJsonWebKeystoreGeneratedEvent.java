package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.springframework.core.io.Resource;

/**
 * This is {@link OidcJsonWebKeystoreGeneratedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ToString(callSuper = true)
@Getter
public class OidcJsonWebKeystoreGeneratedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 8159647975948252375L;

    private final Resource file;

    public OidcJsonWebKeystoreGeneratedEvent(final Object source, final Resource file) {
        super(source);
        this.file = file;
    }
}
