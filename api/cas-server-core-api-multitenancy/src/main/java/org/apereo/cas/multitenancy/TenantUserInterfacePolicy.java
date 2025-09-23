package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * This is {@link TenantUserInterfacePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantUserInterfacePolicy extends Serializable {

    /**
     * Gets theme name.
     *
     * @return the theme name
     */
    String getThemeName();
}
