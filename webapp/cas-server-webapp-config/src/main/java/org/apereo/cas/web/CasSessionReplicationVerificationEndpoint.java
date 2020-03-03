package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This actuator endpoint is meant to be used for testing that session replication is working.
 *
 * All values are put in session and read from session with a prefix so this can't be used
 * to get anything out of the session that wasn't put in the session by this actuator.
 *
 * GET /cas/sessionReplicationVerify - gets session creation millis (not particularly useful)
 * POST /cas/sessionReplicationVerify/testattribute/somevalue - Puts "somevalue" in session under key "testattribute"
 * GET /cas/sessionReplicationVerify/testattribute - Reads value under testattribute from session
 *
 * @author Hal Deadman
 * @since 6.2
 */
@Slf4j
@Endpoint(id = "sessionReplicationVerify", enableByDefault = false)
public class CasSessionReplicationVerificationEndpoint extends BaseCasActuatorEndpoint {

    private static final String TEST_ATTRIBUTE_PREFIX = "CAS_TEST_";

    public CasSessionReplicationVerificationEndpoint(final CasConfigurationProperties casProperties) {
        super(casProperties);
        LOGGER.trace("Creating session replication endpoint, should be used for debug only");
    }

    @ReadOperation
    public String readSessionId() {
        val sessionId = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession(true).getCreationTime();
        LOGGER.trace("Reading session id, creation time: [{}]", sessionId);
        return String.valueOf(sessionId);
    }

    @ReadOperation
    public String readSession(@Selector final String attributeKey) {
        LOGGER.trace("Returning value for attribute key [{}]", attributeKey);
        val session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        val attributeValue = session.getAttribute(TEST_ATTRIBUTE_PREFIX + attributeKey);
        LOGGER.trace("Returning attribute value [{}]", attributeValue);
        return String.valueOf(attributeValue);
    }

    @WriteOperation
    public String writeToSession(@Selector final String attributeKey, @Selector final String attributeValue) {
        LOGGER.trace("Writing attribute [{}] to session with key [{}]", attributeValue, attributeKey);
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession().setAttribute(TEST_ATTRIBUTE_PREFIX + attributeKey, attributeValue);
        return attributeValue + " written.";
    }
}
