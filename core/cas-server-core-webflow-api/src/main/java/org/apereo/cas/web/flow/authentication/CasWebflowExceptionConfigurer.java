package org.apereo.cas.web.flow.authentication;
import module java.base;

/**
 * This is {@link CasWebflowExceptionConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface CasWebflowExceptionConfigurer {
    /**
     * Configure.
     *
     * @param catalog the catalog
     */
    void configure(CasWebflowExceptionCatalog catalog);
}
