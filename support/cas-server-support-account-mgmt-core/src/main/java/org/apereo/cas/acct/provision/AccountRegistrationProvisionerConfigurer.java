package org.apereo.cas.acct.provision;

import org.springframework.core.Ordered;

/**
 * This is {@link AccountRegistrationProvisionerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface AccountRegistrationProvisionerConfigurer extends Ordered {
    /**
     * Configure.
     *
     * @return the account registration provisioner
     */
    AccountRegistrationProvisioner configure();

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
