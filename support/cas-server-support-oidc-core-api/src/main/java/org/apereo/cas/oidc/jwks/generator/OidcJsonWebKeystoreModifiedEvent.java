package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;

import java.io.File;

/**
 * This is {@link OidcJsonWebKeystoreModifiedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ToString(callSuper = true)
@Getter
public class OidcJsonWebKeystoreModifiedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 8059647975948452375L;

    private final File file;

    public OidcJsonWebKeystoreModifiedEvent(final Object source, final File file) {
        super(source);
        this.file = file;
    }
}
