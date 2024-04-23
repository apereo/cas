package org.apereo.cas.authentication.principal;

import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.UUID;

/**
 * This is {@link TestBaseDelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class TestBaseDelegatedClientAuthenticationCredentialResolver extends BaseDelegatedClientAuthenticationCredentialResolver {
    public TestBaseDelegatedClientAuthenticationCredentialResolver(final DelegatedClientAuthenticationConfigurationContext configContext) {
        super(configContext);
    }

    @Override
    public List<DelegatedAuthenticationCandidateProfile> resolve(final RequestContext context,
                                                                 final ClientCredential credentials) {
        val profile = resolveUserProfile(context, credentials).get();
        val p1 = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(profile.getAttributes())
            .id(profile.getId())
            .key(UUID.randomUUID().toString())
            .linkedId("casuser-linked")
            .build();
        return List.of(p1);
    }
}
