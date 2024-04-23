package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;

/**
 * This is {@link AccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface AccountRegistrationProvisioner {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "accountMgmtRegistrationProvisioner";

    /**
     * Provision.
     *
     * @param request the request
     * @return the account registration response
     * @throws Throwable the throwable
     */
    AccountRegistrationResponse provision(AccountRegistrationRequest request) throws Throwable;
}
