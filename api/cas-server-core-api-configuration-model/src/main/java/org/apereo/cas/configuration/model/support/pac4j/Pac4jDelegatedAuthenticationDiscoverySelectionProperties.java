package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link Pac4jDelegatedAuthenticationDiscoverySelectionProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationDiscoverySelectionProperties")
public class Pac4jDelegatedAuthenticationDiscoverySelectionProperties implements Serializable {
    private static final long serialVersionUID = -2561947621312270068L;

    /**
     * Indicate how the selection and presentation of identity providers would be controlled.
     */
    @RequiredProperty
    private Pac4jDelegatedAuthenticationSelectionTypes selectionType = Pac4jDelegatedAuthenticationSelectionTypes.MENU;

    /**
     * Locate discovery settings inside a JSON resource.
     * Only available if {@link #selectionType} is set to {@link Pac4jDelegatedAuthenticationSelectionTypes#DYNAMIC}.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties json = new Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties();

    /**
     * Indicate different types of delegation discovery.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Pac4jDelegatedAuthenticationSelectionTypes {
        /**
         * Defined identity providers will be listed
         * for the user to select.
         */
        MENU(false),

        /**
         * Defined identity providers are pre-built first,
         * and one is chosen dynamically at runtime based on
         * user attributes properties, domain identifier, etc.
         */
        DYNAMIC(true);

        /**
         * Whether selection type is dynamic.
         */
        private final boolean dynamic;
    }
}
