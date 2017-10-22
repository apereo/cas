package org.apereo.cas.support.saml.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseSamlRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseSamlRegisteredServiceAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
    private static final long serialVersionUID = -3301632236702329694L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSamlRegisteredServiceAttributeReleasePolicy.class);

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attributes, final RegisteredService service) {
        if (service instanceof SamlRegisteredService) {
            final SamlRegisteredService saml = (SamlRegisteredService) service;
            final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            if (request == null) {
                LOGGER.warn("Could not locate the request context to process attributes");
                return super.getAttributesInternal(principal, attributes, service);
            }

            String entityId = request.getParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                final String svcParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
                if (StringUtils.isNotBlank(svcParam)) {
                    try {
                        final URIBuilder builder = new URIBuilder(svcParam);
                        entityId = builder.getQueryParams().stream()
                                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                                .map(NameValuePair::getValue)
                                .findFirst()
                                .orElse(StringUtils.EMPTY);
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }

            final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            if (ctx == null) {
                LOGGER.warn("Could not locate the application context to process attributes");
                return super.getAttributesInternal(principal, attributes, service);
            }
            final SamlRegisteredServiceCachingMetadataResolver resolver =
                    ctx.getBean("defaultSamlRegisteredServiceCachingMetadataResolver",
                            SamlRegisteredServiceCachingMetadataResolver.class);

            final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> facade =
                    SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, saml, entityId);

            if (facade == null || !facade.isPresent()) {
                LOGGER.warn("Could not locate metadata for [{}] to process attributes", entityId);
                return super.getAttributesInternal(principal, attributes, service);
            }

            final EntityDescriptor input = facade.get().getEntityDescriptor();
            if (input == null) {
                LOGGER.warn("Could not locate entity descriptor for [{}] to process attributes", entityId);
                return super.getAttributesInternal(principal, attributes, service);
            }
            return getAttributesForSamlRegisteredService(attributes, saml, ctx, resolver, facade.get(), input);
        }
        return super.getAttributesInternal(principal, attributes, service);
    }

    /**
     * Gets attributes for saml registered service.
     *
     * @param attributes         the attributes
     * @param service            the service
     * @param applicationContext the application context
     * @param resolver           the resolver
     * @param facade             the facade
     * @param entityDescriptor   the entity descriptor
     * @return the attributes for saml registered service
     */
    protected abstract Map<String, Object> getAttributesForSamlRegisteredService(Map<String, Object> attributes,
                                                                                 SamlRegisteredService service,
                                                                                 ApplicationContext applicationContext,
                                                                                 SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                                 SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                                 EntityDescriptor entityDescriptor);
}
