package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlIdPMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class SamlIdPMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final OpenSamlConfigBean openSamlConfigBean;

    private final SessionStore distributedSessionStore;

    private final ApplicationContext applicationContext;

    private final CasConfigurationProperties casProperties;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest request,
                                                                   final Service service) {
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val context = new JEEContext(request, response);
        val result = SamlIdPUtils.retrieveSamlRequest(context, distributedSessionStore, openSamlConfigBean, AuthnRequest.class);
        val mappings = getAuthenticationContextMappings();
        return result
            .map(pair -> (AuthnRequest) pair.getLeft())
            .flatMap(authnRequest -> authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()
                .stream()
                .filter(Objects::nonNull)
                .filter(ref -> StringUtils.isNotBlank(ref.getURI()))
                .filter(ref -> {
                    val clazz = ref.getURI();
                    return mappings.containsKey(clazz);
                })
                .findFirst()
                .map(mapped -> mappings.get(mapped.getURI())))
            .flatMap(id -> {
                val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
                return MultifactorAuthenticationUtils.resolveProvider(providerMap, id);
            });
    }

    @Override
    public boolean supports(final HttpServletRequest request, final RegisteredService registeredService,
                            final Authentication authentication, final Service service) {
        if (!getAuthenticationContextMappings().isEmpty()) {
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            val context = new JEEContext(request, response);
            val result = SamlIdPUtils.retrieveSamlRequest(context, distributedSessionStore, openSamlConfigBean, AuthnRequest.class);
            if (result.isPresent()) {
                val authnRequest = (AuthnRequest) result.get().getLeft();
                return authnRequest.getRequestedAuthnContext() != null
                    && authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs() != null
                    && !authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().isEmpty();
            }
        }
        return false;
    }

    /**
     * Gets authentication context mappings.
     *
     * @return the authentication context mappings
     */
    protected Map<String, String> getAuthenticationContextMappings() {
        val authnContexts = casProperties.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings();
        return CollectionUtils.convertDirectedListToMap(authnContexts);
    }
}
