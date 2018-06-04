package org.apereo.cas.configuration.model.core.monitor;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ActuatorEndpointProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-reports", automated = true)
@Getter
@Setter
public class ActuatorEndpointProperties implements Serializable {
    private static final long serialVersionUID = -2463521198550485506L;

    /**
     * Define the security access level for the endpoint.
     */
    public enum EndpointAccessLevel {
        /**
         * Allow open access to the endpoint.
         */
        PERMIT,
        /**
         * Allow anonymous access to the endpoint.
         */
        ANONYMOUS,
        /**
         * Block access to the endpoint.
         */
        DENY,
        /**
         * Require authenticated access to the endpoint.
         */
        AUTHENTICATED,
        /**
         * Require authenticated access to the endpoint along with a role requirement.
         */
        ROLE,
        /**
         * Require authenticated access to the endpoint along with an authority requirement.
         */
        AUTHORITY,
        /**
         * Require authenticated access to the endpoint using a collection of IP addresses.
         */
        IP_ADDRESS
    }

    /**
     * Required user roles.
     */
    private List<String> requiredRoles;

    /**
     * Required user authorities.
     */
    private List<String> requiredAuthorities;

    /**
     * Required ip addresses.
     */
    private List<String> requiredIpAddresses;

    /**
     * Define the security access level of the endpoint.
     */
    private List<EndpointAccessLevel> access = Stream.of(EndpointAccessLevel.DENY).collect(Collectors.toList());
}
