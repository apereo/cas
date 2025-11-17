package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ObjectProvider<@NonNull SamlProfileHandlerConfigurationContext> contextProvider;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest request,
                                                                   final HttpServletResponse response,
                                                                   final Service service) {
        val context = contextProvider.getObject();
        val webContext = new JEEContext(request, response);
        val result = SamlIdPSessionManager.of(context.getOpenSamlConfigBean(), context.getSessionStore()).fetch(webContext, AuthnRequest.class);
        val mappings = getAuthenticationContextMappings();
        return result
            .filter(pair -> registeredService instanceof SamlRegisteredService && pair.getLeft() instanceof AuthnRequest)
            .filter(pair -> isAuthnRequestSigned((SamlRegisteredService) registeredService, request, (AuthnRequest) pair.getLeft(), pair.getRight(), context))
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
                val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(
                    context.getOpenSamlConfigBean().getApplicationContext());
                return MultifactorAuthenticationUtils.resolveProvider(providerMap, id);
            });
    }

    private static Boolean isAuthnRequestSigned(final SamlRegisteredService registeredService, final HttpServletRequest request,
                                                final AuthnRequest authnRequest, final MessageContext messageContext,
                                                final SamlProfileHandlerConfigurationContext context) {
        val isSigned = authnRequest.isSigned() || SAMLBindingSupport.isMessageSigned(messageContext);
        if (isSigned) {
            val entityId = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            val adaptor = SamlRegisteredServiceMetadataAdaptor.get(context.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId).orElseThrow();
            return FunctionUtils.doAndHandle(() -> context.getSamlObjectSignatureValidator()
                .verifySamlProfileRequest(authnRequest, adaptor, request, messageContext));
        }
        return false;
    }

    @Override
    public boolean supports(final HttpServletRequest request, final RegisteredService registeredService,
                            final Authentication authentication, final Service service) {
        if (!getAuthenticationContextMappings().isEmpty() && registeredService instanceof SamlRegisteredService) {
            val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();

            val context = contextProvider.getObject();
            val webContext = new JEEContext(request, response);
            val result = SamlIdPSessionManager.of(context.getOpenSamlConfigBean(),
                context.getSessionStore()).fetch(webContext, AuthnRequest.class);
            if (result.isPresent()) {
                val authnRequest = (AuthnRequest) result.get().getLeft();
                return authnRequest.getRequestedAuthnContext() != null
                    && authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs() != null
                    && !authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().isEmpty();
            }
        }
        return false;
    }

    protected Map<String, String> getAuthenticationContextMappings() {
        val authnContexts = contextProvider.getObject().getCasProperties()
            .getAuthn().getSamlIdp().getCore().getContext().getAuthenticationContextClassMappings();
        return CollectionUtils.convertDirectedListToMap(authnContexts);
    }
}
