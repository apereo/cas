package org.apereo.cas.support.pac4j.authentication.handler.support;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

/**
 * This is {@link DelegatedClientAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DelegatedClientAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private static final List<AuthenticationMethodRef> REFERENCES = List.of(
        new AuthenticationMethodRef("amr", List.of("mfa", "hwk", "swk", "phr", "phrh")),
        new AuthenticationMethodRef("http://schemas.microsoft.com/claims/authnmethodsreferences", List.of("http://schemas.microsoft.com/claims/multipleauthn")));

    private final ServicesManager servicesManager;
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;
    private final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;
    private final CasConfigurationProperties casProperties;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws Throwable {
        val attributes = builder.getPrincipal().getAttributes();
        LOGGER.debug("Delegated authentication principal [{}] has attributes: [{}]",
            builder.getPrincipal().getId(), attributes);

        for (val reference : REFERENCES) {
            val methodName = reference.attribute();
            val methodValues = reference.values();
            if (attributes.containsKey(methodName)) {
                val attrValue = CollectionUtils.toCollection(attributes.get(methodName));
                if (attrValue.stream().anyMatch(methodValues::contains)) {
                    LOGGER.debug("Delegated authentication principal [{}] has attribute [{}] with value(s) [{}]",
                        builder.getPrincipal().getId(), methodName, attrValue);
                    collectAuthenticationContext(builder, transaction);
                }
            }
        }
    }

    @Override
    public boolean supports(final Credential credential) throws Throwable {
        return credential instanceof ClientCredential
            && !multifactorTriggerSelectionStrategy.getMultifactorAuthenticationTriggers().isEmpty();
    }

    protected void collectAuthenticationContext(final AuthenticationBuilder builder,
                                                final AuthenticationTransaction transaction) throws Throwable {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();

        val credential = new BasicIdentifiableCredential(builder.getPrincipal().getId());
        val effectivePrincipal = authenticationSystemSupport.getObject().getPrincipalResolver().resolve(credential);

        val authnResultBuilder = authenticationSystemSupport.getObject().getAuthenticationResultBuilderFactory().newBuilder();
        authnResultBuilder.collect(builder.build());
        authnResultBuilder.collect(DefaultAuthenticationBuilder.newInstance(effectivePrincipal).build());
        val authenticationResult = authnResultBuilder.build(transaction.getService());

        LOGGER.debug("Evaluating multifactor authentication for authentication [{}]", authenticationResult.getAuthentication());
        val registeredService = servicesManager.findServiceBy(transaction.getService());
        val resolvedProvider = multifactorTriggerSelectionStrategy.resolve(
            request, response, registeredService, authenticationResult.getAuthentication(), transaction.getService());
        val providers = resolvedProvider
            .map(provider -> provider instanceof final ChainingMultifactorAuthenticationProvider chain
                ? chain.getMultifactorAuthenticationProviders()
                : List.of(provider))
            .orElseGet(List::of);
        val providerIds = providers.stream().map(MultifactorAuthenticationProvider::getId).collect(Collectors.toList());
        LOGGER.debug("Resolved multifactor providers are [{}]", providerIds);
        if (!providers.isEmpty()) {
            builder.addAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), providerIds);
            LOGGER.info("Collected authentication context attribute for multifactor providers: [{}]", providerIds);
        }
    }

    private record AuthenticationMethodRef(String attribute, List<String> values) {
    }
}
