package org.apereo.cas.util;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.saml.sps.AbstractSamlSPProperties;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link SamlSPUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class SamlSPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlSPUtils.class);

    private SamlSPUtils() {
    }

    /**
     * New saml service provider registration.
     *
     * @param sp the properties
     * @return the saml registered service
     */
    public static SamlRegisteredService newSamlServiceProviderService(final AbstractSamlSPProperties sp) {

        if (StringUtils.isBlank(sp.getMetadata())) {
            LOGGER.debug("Skipped registration of {} since no metadata location is found", sp.getName());
            return null;
        }

        try {
            final SamlRegisteredService service = new SamlRegisteredService();
            service.setName(sp.getName());
            service.setDescription(sp.getDescription());
            final Resource resource = ResourceUtils.prepareClasspathResourceIfNeeded(
                    ResourceUtils.getResourceFrom(sp.getMetadata())
            );
            final String content = IOUtils.toString(resource.getInputStream(), "UTF-8");
            final Matcher m = Pattern.compile("entityID=\"(\\w+)", Pattern.CASE_INSENSITIVE).matcher(content);
            if (m.find()) {
                service.setServiceId(m.group(1));
                LOGGER.debug("Located entityID {} from metadata location {}", service.getServiceId(), sp.getMetadata());
            } else {
                throw new IllegalArgumentException("Could not locate entityID from the supplied metadata file " + sp.getMetadata());
            }

            service.setEvaluationOrder(Integer.MIN_VALUE);
            service.setMetadataLocation(sp.getMetadata());

            final List<String> attributesToRelease = new ArrayList<>(sp.getAttributes());
            if (StringUtils.isNotBlank(sp.getNameIdAttribute())) {
                attributesToRelease.add(sp.getNameIdAttribute());
                service.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider(sp.getNameIdAttribute()));
            }
            service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(attributesToRelease));

            return service;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Save service only if it's not already found in the registry.
     *
     * @param service         the service
     * @param servicesManager the services manager
     */
    public static void saveService(final RegisteredService service, final ServicesManager servicesManager) {
        servicesManager.load();
        if (!servicesManager.matchesExistingService(service.getServiceId())) {
            LOGGER.debug("Service {} does not exist in the registry and will be added.", service.getServiceId());
            servicesManager.save(service);
            servicesManager.load();
        } else {
            LOGGER.debug("Service {} exists in the registry and will not be added again.", service.getServiceId());
        }
    }
}
