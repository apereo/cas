package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link ScimAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class ScimAccountRegistrationProvisioner implements AccountRegistrationProvisioner {

    private final PrincipalProvisioner principalProvisioner;

    private final PrincipalFactory principalFactory;

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        val attributes = new LinkedHashMap<String, List<Object>>();
        request.asMap().forEach((key, value) -> attributes.put(key, CollectionUtils.wrapList(value)));
        val principal = principalFactory.createPrincipal(request.getUsername(), attributes);
        val credential = new UsernamePasswordCredential(request.getUsername(), request.getPassword());
        val result = principalProvisioner.provision(principal, credential);
        LOGGER.debug("Provisioned account registration request for [{}]: [{}]", request.getUsername(),
            BooleanUtils.toString(result, "success", "failure"));
        return result ? AccountRegistrationResponse.success() : new AccountRegistrationResponse();
    }
}
