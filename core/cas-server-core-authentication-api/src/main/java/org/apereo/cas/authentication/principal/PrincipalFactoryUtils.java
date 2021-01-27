package org.apereo.cas.authentication.principal;

import org.apereo.cas.configuration.model.RestEndpointProperties;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

/**
 * This is {@link PrincipalFactoryUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class PrincipalFactoryUtils {

    /**
     * New principal factory.
     *
     * @return the principal factory
     */
    public static PrincipalFactory newPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    /**
     * New principal factory.
     *
     * @param resource the resource
     * @return the principal factory
     */
    public static PrincipalFactory newGroovyPrincipalFactory(final Resource resource) {
        return new GroovyPrincipalFactory(resource);
    }

    /**
     * New restful principal factory .
     *
     * @param properties the properties
     * @return the principal factory
     */
    public static PrincipalFactory newRestfulPrincipalFactory(final RestEndpointProperties properties) {
        return new RestfulPrincipalFactory(properties);
    }
}
