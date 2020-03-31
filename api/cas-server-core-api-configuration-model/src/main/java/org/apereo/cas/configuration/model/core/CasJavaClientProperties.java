package org.apereo.cas.configuration.model.core;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasJavaClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class CasJavaClientProperties implements Serializable {
    private static final long serialVersionUID = -3646242105668747303L;
    /**
     * Prefix of the CAS server used to establish ticket validators for the client.
     * Typically set to {@code https://sso.example.org/cas}
     */
    private String prefix;
    /**
     * Determines the type of ticket validator that CAS should create from the Java CAS client
     * when attempting to issue in-bound ticket validation calls.
     */
    private ClientTicketValidatorTypes validatorType = ClientTicketValidatorTypes.CAS30;

    /**
     * The enum Client ticket validator types.
     */
    public enum ClientTicketValidatorTypes {
        /**
         * CAS10 ticket validator.
         */
        CAS10,
        /**
         * CAS20 ticket validator.
         */
        CAS20,
        /**
         * CAS30 ticket validator.
         */
        CAS30
    }
}
