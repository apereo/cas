package org.apereo.cas.support.saml.services;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ApplicationContext;

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

    /**
     * Gets entity id from request.
     *
     * @param selectedService the selected service
     * @return the entity id from request
     */
    protected static String getEntityIdFromRequest(final Service selectedService) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        if (request == null || selectedService == null) {
            LOGGER.debug("No http request could be identified to locate the entity id");
            return null;
        }
        LOGGER.debug("Attempting to determine entity id for service [{}]", selectedService);
        val entityIdAttribute = selectedService.getAttributes().get(SamlProtocolConstants.PARAMETER_ENTITY_ID);
        if (entityIdAttribute != null && !entityIdAttribute.isEmpty()) {
            LOGGER.debug("Found entity id [{}] as a service attribute", entityIdAttribute);
            return CollectionUtils.firstElement(entityIdAttribute).map(Object::toString).orElseThrow();
        }
        val providerIdAttribute = selectedService.getAttributes().get(SamlIdPConstants.PROVIDER_ID);
        if (providerIdAttribute != null && !providerIdAttribute.isEmpty()) {
            LOGGER.debug("Found provider entity id [{}] as a service attribute", providerIdAttribute);
            return CollectionUtils.firstElement(providerIdAttribute).map(Object::toString).orElseThrow();
        }
        val samlRequest = selectedService.getAttributes().get(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
        if (samlRequest != null && !samlRequest.isEmpty()) {
            val applicationContext = ApplicationContextProvider.getApplicationContext();
            val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME,
                SamlRegisteredServiceCachingMetadataResolver.class);
            val attributeValue = CollectionUtils.firstElement(samlRequest).map(Object::toString).orElseThrow();
            val openSamlConfigBean = resolver.getOpenSamlConfigBean();
            val authnRequest = SamlIdPUtils.retrieveSamlRequest(openSamlConfigBean, RequestAbstractType.class, attributeValue);
            SamlUtils.logSamlObject(openSamlConfigBean, authnRequest);
            val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            LOGGER.debug("Found entity id [{}] from SAML request issuer", issuer);
            return issuer;
        }
        val entityId = request.getParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);
        if (StringUtils.isNotBlank(entityId)) {
            LOGGER.debug("Found entity id [{}] as a request parameter", entityId);
            return entityId;
        }
        val svcParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        if (StringUtils.isNotBlank(svcParam)) {
            try {
                val builder = new URIBuilder(svcParam);
                return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .map(NameValuePair::getValue)
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
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
        val sessionStore = applicationContext.getBean(DistributedJEESessionStore.DEFAULT_BEAN_NAME, SessionStore.class);
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val context = new JEEContext(request, response);
        val result = SamlIdPUtils.retrieveSamlRequest(context, sessionStore, openSamlConfigBean, AuthnRequest.class);
        val authnRequest = (AuthnRequest) result
            .orElseThrow(() -> new IllegalArgumentException("SAML request could not be determined from session store"))
            .getLeft();
        return Optional.of(authnRequest);
    }

    /**
     * Determine service provider metadata facade.
     *
     * @param registeredService the registered service
     * @param entityId          the entity id
     * @return the optional
     */
    @JsonIgnore
    protected static Optional<SamlRegisteredServiceServiceProviderMetadataFacade> determineServiceProviderMetadataFacade(
        final SamlRegisteredService registeredService, final String entityId) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME,
            SamlRegisteredServiceCachingMetadataResolver.class);
        return SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, registeredService, entityId);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal,
                                                           final Map<String, List<Object>> attributes,
                                                           final RegisteredService registeredService,
                                                           final Service selectedService) {
        if (registeredService instanceof SamlRegisteredService) {
            val samlRegisteredService = (SamlRegisteredService) registeredService;

            val applicationContext = ApplicationContextProvider.getApplicationContext();
            val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME,
                SamlRegisteredServiceCachingMetadataResolver.class);
            val entityId = getEntityIdFromRequest(selectedService);
            val facade = determineServiceProviderMetadataFacade(samlRegisteredService, entityId);

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
    protected abstract Map<String, List<Object>> getAttributesForSamlRegisteredService(
        Map<String, List<Object>> attributes,
        SamlRegisteredService registeredService,
        ApplicationContext applicationContext,
        SamlRegisteredServiceCachingMetadataResolver resolver,
        SamlRegisteredServiceServiceProviderMetadataFacade facade,
        EntityDescriptor entityDescriptor,
        Principal principal,
        Service selectedService);
}
