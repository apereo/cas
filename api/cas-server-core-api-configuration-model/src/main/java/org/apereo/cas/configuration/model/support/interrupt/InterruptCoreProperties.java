package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link InterruptCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("InterruptCoreProperties")
public class InterruptCoreProperties implements Serializable {
    private static final long serialVersionUID = 4263941933003310968L;

    /**
     * Whether execution of the interrupt inquiry
     * query should be always forced, and the status
     * of interrupt check should be ignored. This is a global setting
     * that can optionally be overruled for each application policy.
     */
    private boolean forceExecution;

    /**
     * Define how interrupt notifications should be
     * triggered in the authentication flow.
     */
    private InterruptTriggerModes triggerMode = InterruptTriggerModes.AFTER_AUTHENTICATION;

    /**
     * Enumerate the trigger modes for
     * interrupt authentication.
     */
    public enum InterruptTriggerModes {
        /**
         * Trigger interrupt notifications and inquiry
         * after authentication events and before
         * single sign-on has been established.
         */
        AFTER_AUTHENTICATION,
        /**
         * Trigger interrupt notifications and inquiry
         * after single sign-on has been established.
         * Interrupt queries that execute after single sign-on
         * cannot control the creation of the
         * SSO session conditionally.
         */
        AFTER_SSO
    }
}
