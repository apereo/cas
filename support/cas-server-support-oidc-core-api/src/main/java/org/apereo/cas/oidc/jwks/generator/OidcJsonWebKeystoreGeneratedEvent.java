package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import org.springframework.core.io.Resource;

import java.io.Serial;

/**
 * This is {@link OidcJsonWebKeystoreGeneratedEvent}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ToString(callSuper = true)
@Getter
public class OidcJsonWebKeystoreGeneratedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 8159647975948252375L;

    private final Resource file;

    public OidcJsonWebKeystoreGeneratedEvent(final Object source, final Resource file, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.file = file;
    }
}
