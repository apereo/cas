package org.apereo.cas.util.feature;

import java.util.List;

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
     */
    List<CasRuntimeModule> load();
}
