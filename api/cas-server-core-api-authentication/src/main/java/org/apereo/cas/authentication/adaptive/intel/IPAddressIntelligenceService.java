package org.apereo.cas.authentication.adaptive.intel;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link IPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface IPAddressIntelligenceService {

    /**
     * Examine ip address and produce an intelligence response.
     *
     * @param context         the context
     * @param clientIpAddress the client ip address
     * @return the ip address intelligence response
     */
    IPAddressIntelligenceResponse examine(RequestContext context, String clientIpAddress);

    /**
     * NoOp ip address intelligence service.
     *
     * @return the ip address intelligence service
     */
    static IPAddressIntelligenceService allowed() {
        return (context, clientIpAddress) -> IPAddressIntelligenceResponse.allowed();
    }

    /**
     * Banned ip address intelligence service.
     *
     * @return the ip address intelligence service
     */
    static IPAddressIntelligenceService banned() {
        return (context, clientIpAddress) -> IPAddressIntelligenceResponse.banned();
    }
}
