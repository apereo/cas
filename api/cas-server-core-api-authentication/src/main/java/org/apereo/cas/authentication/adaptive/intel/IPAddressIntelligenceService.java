package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.util.NamedObject;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link IPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface IPAddressIntelligenceService extends NamedObject {

    /**
     * The default bean name.
     */
    String BEAN_NAME = "ipAddressIntelligenceService";

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

    /**
     * Examine ip address and produce an intelligence response.
     *
     * @param context         the context
     * @param clientIpAddress the client ip address
     * @return the ip address intelligence response
     * @throws Throwable the throwable
     */
    IPAddressIntelligenceResponse examine(RequestContext context, String clientIpAddress) throws Throwable;
}
