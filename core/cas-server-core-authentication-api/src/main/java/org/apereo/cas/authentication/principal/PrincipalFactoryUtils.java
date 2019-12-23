package org.apereo.cas.authentication.principal;

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
     * @param url      the url
     * @param username the username
     * @param password the password
     * @return the principal factory
     */
    public static PrincipalFactory newRestfulPrincipalFactory(final String url, final String username, final String password) {
        return new RestfulPrincipalFactory(url, username, password);
    }

    /**
     * New restful principal factory.
     *
     * @param url the url
     * @return the principal factory
     */
    public static PrincipalFactory newRestfulPrincipalFactory(final String url) {
        return newRestfulPrincipalFactory(url, null, null);
    }
}
