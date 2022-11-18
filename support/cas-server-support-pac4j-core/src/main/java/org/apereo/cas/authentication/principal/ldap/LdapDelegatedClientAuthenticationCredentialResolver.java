package org.apereo.cas.authentication.principal.ldap;

import org.apereo.cas.authentication.principal.BaseDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.execution.RequestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is {@link LdapDelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class LdapDelegatedClientAuthenticationCredentialResolver extends BaseDelegatedClientAuthenticationCredentialResolver
    implements DisposableBean {
    private final ConnectionFactory connectionFactory;

    public LdapDelegatedClientAuthenticationCredentialResolver(
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final ConnectionFactory connectionFactory) {
        super(configContext);
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<DelegatedAuthenticationCandidateProfile> resolve(final RequestContext context, final ClientCredential credentials) {
        return FunctionUtils.doUnchecked(() -> {
            val profile = resolveUserProfile(context, credentials).get();
            val properties = configContext.getCasProperties().getAuthn().getPac4j().getProfileSelection().getLdap();
            try (val factory = new LdapConnectionFactory(connectionFactory)) {
                val filter = LdapUtils.newLdaptiveSearchFilter(properties.getSearchFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    List.of(profile.getId()));
                LOGGER.debug("Fetching user attributes [{}] for [{}] via [{}]", properties.getAttributes(), profile, filter);
                val result = factory.executeSearchOperation(properties.getBaseDn(), filter, 0,
                    properties.getAttributes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                LOGGER.debug("Found entries: [{}]", result.getEntries().size());
                return result
                    .getEntries()
                    .stream()
                    .map(entry -> {
                        LOGGER.trace("Found entry [{}]", entry);
                        var attributes = new HashMap<>(profile.getAttributes());
                        for (val attr : entry.getAttributes()) {
                            attributes.put(attr.getName(), attr.getStringValues());
                        }
                        val name = Optional.ofNullable(entry.getAttribute(properties.getProfileIdAttribute()))
                            .map(LdapAttribute::getStringValue)
                            .orElseGet(profile::getId);
                        LOGGER.debug("Adding attributes [{}] to the selected profile: [{}]", attributes, name);
                        return DelegatedAuthenticationCandidateProfile.builder()
                            .attributes(attributes)
                            .id(name)
                            .key(UUID.randomUUID().toString())
                            .linkedId(profile.getId())
                            .build();
                    }).collect(Collectors.toList());
            }
        });
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

}
