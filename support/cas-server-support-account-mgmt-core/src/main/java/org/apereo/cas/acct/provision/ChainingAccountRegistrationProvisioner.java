package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;

import lombok.RequiredArgsConstructor;
import lombok.val;
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

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        val aggregate = new AccountRegistrationResponse();
        provisioners.forEach(Unchecked.consumer(p -> aggregate.collect(p.provision(request))));
        return aggregate;
    }
}
