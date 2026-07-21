package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.MissingSamlAuthnRequestException;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;

/**
 * This is {@link SamlIdPSAttributeReleasePolicyUtils}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@UtilityClass
@Slf4j
class SamlIdPSAttributeReleasePolicyUtils {

    public static @Nullable String getEntityIdFromRequest(final RegisteredServiceAttributeReleasePolicyContext context) {
        val selectedService = context.getService();
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        if (request == null || selectedService == null) {
            LOGGER.debug("No http request could be identified to locate the entity id");
            return null;
        }
        LOGGER.debug("Attempting to determine entity id for service [{}]", selectedService);
        val entityIdAttribute = selectedService.getAttributeAs(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.class);
        if (entityIdAttribute != null && !entityIdAttribute.isEmpty()) {
            LOGGER.debug("Found entity id [{}] as a service attribute", entityIdAttribute);
            return CollectionUtils.firstElement(entityIdAttribute).map(Object::toString).orElseThrow();
        }
        val providerIdAttribute = selectedService.getAttributeAs(SamlIdPConstants.PROVIDER_ID, List.class);
        if (providerIdAttribute != null && !providerIdAttribute.isEmpty()) {
            LOGGER.debug("Found provider entity id [{}] as a service attribute", providerIdAttribute);
            return CollectionUtils.firstElement(providerIdAttribute).map(Object::toString).orElseThrow();
        }
        val samlRequest = selectedService.getAttributeAs(SamlProtocolConstants.PARAMETER_SAML_REQUEST, List.class);
        if (samlRequest != null && !samlRequest.isEmpty()) {
            val applicationContext = context.getApplicationContext();
            val sessionStore = applicationContext.getBean("samlIdPDistributedSessionStore", SessionStore.class);

            val resolver = applicationContext.getBean(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME,
                SamlRegisteredServiceCachingMetadataResolver.class);
            val attributeValue = CollectionUtils.firstElement(samlRequest).map(Object::toString).orElseThrow();
            val openSamlConfigBean = resolver.getOpenSamlConfigBean();
            val authnRequest = SamlIdPSessionManager.of(openSamlConfigBean, sessionStore)
                .fetch(RequestAbstractType.class, attributeValue);
            openSamlConfigBean.logObject(authnRequest);
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
        return FunctionUtils.doIf(StringUtils.isNotBlank(svcParam),
            () -> FunctionUtils.doAndHandle(_ -> {
                val builder = new URIBuilder(svcParam);
                return builder.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .map(NameValuePair::getValue)
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
            }, throwable -> {
                LoggingUtils.error(LOGGER, throwable);
                return null;
            })
                .apply(svcParam),
            () -> null).get();
    }

    public static Optional<AuthnRequest> getSamlAuthnRequest(final RegisteredServiceAttributeReleasePolicyContext context) {
        val openSamlConfigBean = context.getApplicationContext().getBean(OpenSamlConfigBean.DEFAULT_BEAN_NAME, OpenSamlConfigBean.class);
        val sessionStore = context.getApplicationContext().getBean("samlIdPDistributedSessionStore", SessionStore.class);
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        val webContext = new JEEContext(request, response);
        val result = SamlIdPSessionManager.of(openSamlConfigBean, sessionStore)
            .fetch(webContext, AuthnRequest.class);
        val authnRequest = (AuthnRequest) result
            .orElseThrow(() -> {
                val samlAuthnRequestId = webContext.getRequestParameter(SamlIdPConstants.AUTHN_REQUEST_ID).orElse("N/A");
                return new MissingSamlAuthnRequestException("SAML2 request could not be determined from session store %s for SAML2 request id %s"
                    .formatted(sessionStore.getClass().getName(), samlAuthnRequestId));
            })
            .getLeft();
        return Optional.of(authnRequest);
    }

    @JsonIgnore
    public static Optional<SamlRegisteredServiceMetadataAdaptor> determineServiceProviderMetadataFacade(
        final RegisteredServiceAttributeReleasePolicyContext context, final String entityId) {
        val resolver = context.getApplicationContext().getBean(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME,
            SamlRegisteredServiceCachingMetadataResolver.class);
        return SamlRegisteredServiceMetadataAdaptor.get(resolver, (SamlRegisteredService) context.getRegisteredService(), entityId);
    }
}
