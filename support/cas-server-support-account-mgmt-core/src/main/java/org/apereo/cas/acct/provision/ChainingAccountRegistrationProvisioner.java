package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;

import java.util.List;

/**
 * This is {@link ChainingAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class ChainingAccountRegistrationProvisioner implements AccountRegistrationProvisioner {
    private final List<AccountRegistrationProvisioner> provisioners;

    @Audit(action = AuditableActions.ACCOUNT_REGISTRATION,
        actionResolverName = AuditActionResolvers.ACCOUNT_REGISTRATION_PROVISIONING_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.ACCOUNT_REGISTRATION_PROVISIONING_RESOURCE_RESOLVER)
    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        val aggregate = new AccountRegistrationResponse();
        provisioners.forEach(Unchecked.consumer(p -> aggregate.collect(p.provision(request))));
        return aggregate;
    }
}
