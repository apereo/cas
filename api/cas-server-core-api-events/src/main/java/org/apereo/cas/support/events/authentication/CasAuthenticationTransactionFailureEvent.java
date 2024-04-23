package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link CasAuthenticationTransactionFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class CasAuthenticationTransactionFailureEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 8059647975948452375L;

    private final Map<String, Throwable> failures;

    private final Collection<Credential> credential;


    public CasAuthenticationTransactionFailureEvent(final Object source, final Map<String, Throwable> failures,
                                                    final Collection<Credential> credential, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.failures = failures;
        this.credential = credential;
    }

    public Credential getCredential() {
        return credential.iterator().next();
    }
}
