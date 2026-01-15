package org.apereo.cas.support.events.authentication;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationTransaction;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasAuthenticationPolicyFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasAuthenticationPolicyFailureEvent extends CasAuthenticationTransactionFailureEvent {
    @Serial
    private static final long serialVersionUID = 2208076621158767073L;

    private final Authentication authentication;


    public CasAuthenticationPolicyFailureEvent(final Object source,
                                               final Map<String, Throwable> failures,
                                               final AuthenticationTransaction transaction,
                                               final Authentication authentication, final ClientInfo clientInfo) {
        super(source, failures, transaction.getCredentials(), clientInfo);
        this.authentication = authentication;
    }
}
