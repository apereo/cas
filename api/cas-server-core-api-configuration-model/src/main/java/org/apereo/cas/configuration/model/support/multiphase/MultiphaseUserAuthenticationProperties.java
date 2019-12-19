package org.apereo.cas.configuration.model.support.multiphase;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link MultiphaseAuthenticationProperties};
 * mostly a skeleton for the time being as it's either on or off.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-multiphase")
@Getter
@Setter
public class MultiphaseAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 3113258705759426332L;

    /**
     * Indicates whether this should do anything.
     */
    private boolean enabled;

}
