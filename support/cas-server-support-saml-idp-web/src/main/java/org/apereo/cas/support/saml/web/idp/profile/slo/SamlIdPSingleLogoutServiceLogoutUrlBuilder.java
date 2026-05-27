package org.apereo.cas.support.saml.web.idp.profile.slo;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.SamlIdPServiceAttributeExtractor;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.UrlValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link SamlIdPSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlIdPSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    /**
     * Property to indicate the binding for the saml logout profile.
     */
    public static final String PROPERTY_NAME_SINGLE_LOGOUT_BINDING = "singleLogoutSamlBinding";

    protected final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    protected final List<String> logoutRequestBindings;

    public SamlIdPSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                      final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                      final UrlValidator urlValidator,
                                                      final SamlIdPProperties samlIdPProperties) {
        super(servicesManager, urlValidator);
        this.samlRegisteredServiceCachingMetadataResolver = resolver;
        this.logoutRequestBindings = samlIdPProperties.getLogout().getLogoutRequestBindings();
    }

    @Override
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return super.supports(registeredService, singleLogoutService, httpRequest)
            && registeredService instanceof SamlRegisteredService
            && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, singleLogoutService)
            && buildLogoutUrl(registeredService, singleLogoutService) != null;
    }

    @Override
    public Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                          final WebApplicationService singleLogoutService,
                                                          final Optional<HttpServletRequest> httpRequest) {
        try {
            val location = buildLogoutUrl(registeredService, singleLogoutService);
            if (location != null) {
                LOGGER.debug("Final logout URL built for [{}] is [{}]", registeredService, location);
                return CollectionUtils.wrap(location);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.trace("Service [{}] is not a SAML service, or its logout url could not be determined", registeredService);
        return new ArrayList<>();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private @Nullable SingleLogoutUrl buildLogoutUrl(final RegisteredService registeredService,
                                                     final WebApplicationService singleLogoutService) {
        LOGGER.trace("Building logout url for SAML service [{}]", registeredService);

        val samlRegisteredService = (SamlRegisteredService) registeredService;
        val extractionResult = SamlIdPServiceAttributeExtractor.extract(registeredService, singleLogoutService);
        val entityID = extractionResult
            .map(pair -> {
                val attribute = pair.getLeft();
                val attributeValue = pair.getRight();
                LOGGER.trace("Located service attribute [{}] with value [{}]", attribute, attributeValue);
                return attribute.getEntityIdFrom(samlRegisteredServiceCachingMetadataResolver, attributeValue);
            })
            .orElseGet(singleLogoutService::getId);
        LOGGER.trace("Located entity id [{}]", entityID);

        val adaptorRes = SamlRegisteredServiceMetadataAdaptor.get(
            samlRegisteredServiceCachingMetadataResolver, samlRegisteredService, entityID);
        if (adaptorRes.isEmpty()) {
            LOGGER.warn("Cannot find metadata linked to [{}]", entityID);
            return null;
        }
        val adaptor = adaptorRes.get();
        for (val binding : this.logoutRequestBindings) {
            var sloService = adaptor.getSingleLogoutService(binding);
            if (sloService != null) {
                return finalizeSingleLogoutUrl(sloService, samlRegisteredService);
            }
        }
        LOGGER.warn("Cannot find SLO service in metadata for entity id [{}]", entityID);
        return null;
    }

    private static @Nullable SingleLogoutUrl finalizeSingleLogoutUrl(final SingleLogoutService sloService, final SamlRegisteredService service) {
        val location = StringUtils.isBlank(sloService.getResponseLocation())
            ? sloService.getLocation()
            : sloService.getResponseLocation();
        if (StringUtils.isNotBlank(location)) {
            val url = new SingleLogoutUrl(location, service.getLogoutType());
            url.getProperties().put(PROPERTY_NAME_SINGLE_LOGOUT_BINDING, sloService.getBinding());
            return url;
        }
        return null;
    }
}
