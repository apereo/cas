package org.apereo.cas.util.feature;

import module java.base;

/**
 * This is {@link CasRuntimeModuleLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface CasRuntimeModuleLoader {

    /**
     * Load modules.
     *
     * @return the list
     * @throws Exception the exception
     */
    List<CasRuntimeModule> load() throws Exception;
}
