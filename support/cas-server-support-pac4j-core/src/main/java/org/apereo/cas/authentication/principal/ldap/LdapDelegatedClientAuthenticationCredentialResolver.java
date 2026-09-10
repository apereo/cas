package org.apereo.cas.authentication.principal.ldap;

import module java.base;
import org.apereo.cas.authentication.principal.BaseDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationLdapProfileSelectionProperties;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.ldaptive.LdapAttribute;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link LdapDelegatedClientAuthenticationCredentialResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class LdapDelegatedClientAuthenticationCredentialResolver extends BaseDelegatedClientAuthenticationCredentialResolver implements DisposableBean {

    private final Map<String, LdapConnectionFactory> connectionFactories;

    public LdapDelegatedClientAuthenticationCredentialResolver(final DelegatedClientAuthenticationConfigurationContext configContext) {
        super(configContext);
        this.connectionFactories = new LinkedHashMap<>();
        configContext.getCasProperties().getAuthn().getPac4j().getProfileSelection().getLdap()
            .forEach(ldap -> connectionFactories.computeIfAbsent(ldap.toStableIdentifier(),
                _ -> new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap))));
    }

    @Override
    public void destroy() {
        connectionFactories.values().forEach(LdapConnectionFactory::close);
    }

    @Override
    public @Nullable List<DelegatedAuthenticationCandidateProfile> resolve(final RequestContext context, final ClientCredential credentials) {
        val profile = resolveUserProfile(context, credentials).orElseThrow();
        return configContext.getCasProperties().getAuthn().getPac4j().getProfileSelection().getLdap()
            .stream()
            .map(Unchecked.function(props -> queryLdap(props, profile)))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    protected List<DelegatedAuthenticationCandidateProfile> queryLdap(final Pac4jDelegatedAuthenticationLdapProfileSelectionProperties ldap,
                                                                      final UserProfile profile) throws Exception {

        LOGGER.debug("Configured LDAP delegated authentication profile selection via [{}]", ldap.getLdapUrl());
        val factory = connectionFactories.get(ldap.toStableIdentifier());
        val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            List.of(profile.getId()));
        LOGGER.debug("Fetching user attributes [{}] for [{}] via [{}]", ldap.getAttributes(), profile, filter);
        val result = factory.executeSearchOperation(ldap.getBaseDn(), filter, 0,
            ldap.getAttributes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
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
                val name = Optional.ofNullable(entry.getAttribute(ldap.getProfileIdAttribute()))
                    .map(LdapAttribute::getStringValue)
                    .orElseGet(profile::getId);
                LOGGER.debug("Adding attributes [{}] to the selected profile: [{}]", attributes, name);
                return DelegatedAuthenticationCandidateProfile.builder()
                    .attributes(attributes)
                    .id(name)
                    .key(UUID.randomUUID().toString())
                    .linkedId(profile.getId())
                    .build();
            })
            .collect(Collectors.toList());
    }

}
