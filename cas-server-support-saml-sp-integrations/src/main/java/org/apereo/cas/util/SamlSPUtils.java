package org.apereo.cas.util;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
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
     * New saml service provider service saml registered service.
     *
     * @param name             the name
     * @param description      the description
     * @param metadataLocation the metadata location
     * @param attributes       the attributes
     * @return the saml registered service
     */
    public static SamlRegisteredService newSamlServiceProviderService(final String name,
                                                                      final String description,
                                                                      final String metadataLocation,
                                                                      final List<String> attributes) {
        return newSamlServiceProviderService(name, description, metadataLocation, null, attributes);
    }
    
    /**
     * New saml service provider service saml registered service.
     *
     * @param name             the name
     * @param description      the description
     * @param metadataLocation the metadata location
     * @param userIdAttribute  the user id attribute
     * @return the saml registered service
     */
    public static SamlRegisteredService newSamlServiceProviderService(final String name,
                                                                      final String description,
                                                                      final String metadataLocation,
                                                                      final String userIdAttribute) {
        return newSamlServiceProviderService(name, description, metadataLocation, userIdAttribute, new ArrayList<>());
    }
    
    /**
     * New saml service provider registration.
     *
     * @param name             the name
     * @param description      the description
     * @param metadataLocation the metadata location
     * @param userIdAttribute  the user id attribute
     * @param attributes       the attributes
     * @return the saml registered service
     */
    public static SamlRegisteredService newSamlServiceProviderService(final String name,
                                                                      final String description,
                                                                      final String metadataLocation,
                                                                      final String userIdAttribute,
                                                                      final List<String> attributes) {

        if (StringUtils.isBlank(metadataLocation)) {
            LOGGER.debug("Skipped registration of {} since no metadata location is found", name);
            return null;
        }
        
        try {
            final SamlRegisteredService service = new SamlRegisteredService();
            service.setName(name);
            service.setDescription(description);
            final Resource resource = ResourceUtils.prepareClasspathResourceIfNeeded(
                    ResourceUtils.getResourceFrom(metadataLocation)
            );
            final String content = IOUtils.toString(resource.getInputStream(), "UTF-8");
            final Matcher m = Pattern.compile("entityID=\"(\\w+)", Pattern.CASE_INSENSITIVE).matcher(content);
            if (m.find()) {
                service.setServiceId(m.group(1));
                LOGGER.debug("Located entityID {} from metadata location {}", service.getServiceId(), metadataLocation);
            } else {
                throw new IllegalArgumentException("Could not locate entityID from the supplied metadata file " + metadataLocation);
            }

            service.setEvaluationOrder(Integer.MIN_VALUE);
            service.setMetadataLocation(metadataLocation);

            final List<String> attributesToRelease = new ArrayList<>(attributes);
            if (StringUtils.isNotBlank(userIdAttribute)) {
                attributesToRelease.add(userIdAttribute);
                service.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider(userIdAttribute));
            }
            service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(attributesToRelease));
            
            return service;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
