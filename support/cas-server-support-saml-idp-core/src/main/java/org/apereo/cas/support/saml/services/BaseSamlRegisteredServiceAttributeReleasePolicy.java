package org.apereo.cas.support.saml.services;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseSamlRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class BaseSamlRegisteredServiceAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
    private static final long serialVersionUID = -3301632236702329694L;

    @SneakyThrows
    private static String getEntityIdFromRequest(final HttpServletRequest request) {
        val entityId = request.getParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);
        if (StringUtils.isNotBlank(entityId)) {
            return entityId;
        }
        val svcParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        if (StringUtils.isNotBlank(svcParam)) {
            val builder = new URIBuilder(svcParam);
            return builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(StringUtils.EMPTY);
        }
        return null;
    }

    /**
     * Gets saml authn request.
     *
     * @param applicationContext the application context
     * @return the saml authn request
     */
    protected static Optional<AuthnRequest> getSamlAuthnRequest(final ApplicationContext applicationContext) {
        val openSamlConfigBean = applicationContext.getBean(OpenSamlConfigBean.DEFAULT_BEAN_NAME, OpenSamlConfigBean.class);
        val sessionStore = applicationContext.getBean("samlIdPDistributedSessionStore", SessionStore.class);
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val context = new JEEContext(request, response);

        val result = SamlIdPUtils.retrieveSamlRequest(context, sessionStore, openSamlConfigBean, AuthnRequest.class);
        val authnRequest = (AuthnRequest) result
            .orElseThrow(() -> new IllegalArgumentException("SAML request could not be determined from session store"))
            .getLeft();
        return Optional.of(authnRequest);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal,
                                                           final Map<String, List<Object>> attributes,
                                                           final RegisteredService registeredService,
                                                           final Service selectedService) {
        if (registeredService instanceof SamlRegisteredService) {
            val samlRegisteredService = (SamlRegisteredService) registeredService;

            val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val entityId = getEntityIdFromRequest(request);
            val applicationContext = ApplicationContextProvider.getApplicationContext();
            val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME,
                SamlRegisteredServiceCachingMetadataResolver.class);
            val facade = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlRegisteredService, entityId);

            if (facade.isEmpty()) {
                LOGGER.warn("Could not locate metadata for [{}] to process attributes", entityId);
                return new HashMap<>(0);
            }

            val entityDescriptor = facade.get().getEntityDescriptor();
            return getAttributesForSamlRegisteredService(attributes, samlRegisteredService, applicationContext,
                resolver, facade.get(), entityDescriptor, principal, selectedService);
        }
        return authorizeReleaseOfAllowedAttributes(principal, attributes, registeredService, selectedService);
    }

    /**
     * Gets attributes for saml registered service.
     *
     * @param attributes         the attributes
     * @param registeredService  the service
     * @param applicationContext the application context
     * @param resolver           the resolver
     * @param facade             the facade
     * @param entityDescriptor   the entity descriptor
     * @param principal          the principal
     * @param selectedService    the selected service
     * @return the attributes for saml registered service
     */
    protected abstract Map<String, List<Object>> getAttributesForSamlRegisteredService(Map<String, List<Object>> attributes,
                                                                                       SamlRegisteredService registeredService,
                                                                                       ApplicationContext applicationContext,
                                                                                       SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                       SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                                       EntityDescriptor entityDescriptor,
                                                                                       Principal principal,
                                                                                       Service selectedService);
}
