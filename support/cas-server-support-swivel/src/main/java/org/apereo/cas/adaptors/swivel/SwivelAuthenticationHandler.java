package org.apereo.cas.adaptors.swivel;

import com.swiveltechnologies.pinsafe.client.agent.AgentXmlRequest;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SwivelAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwivelAuthenticationHandler.class);
    private static final String SWIVEL_ERR_CODE_AUTHN_FAIL = "swivel.server.error";
    private static final Map<String, String> ERROR_MAP = createErrorCodeMap();

    private final SwivelMultifactorProperties swivelProperties;

    public SwivelAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                       final PrincipalFactory principalFactory,
                                       final SwivelMultifactorProperties swivelProperties) {
        super(name, servicesManager, principalFactory, null);
        this.swivelProperties = swivelProperties;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final SwivelCredential swivelCredential = (SwivelCredential) credential;
        if (swivelCredential == null || StringUtils.isBlank(swivelCredential.getToken())) {
            throw new IllegalArgumentException("No credential could be found or credential token is blank");
        }
        final RequestContext context = RequestContextHolder.getRequestContext();
        if (context == null) {
            throw new IllegalArgumentException("No request context could be found to locate an authentication event");
        }
        final Authentication authentication = WebUtils.getAuthentication(context);
        if (authentication == null) {
            throw new IllegalArgumentException("Request context has no reference to an authentication event to locate a principal");
        }
        final String uid = authentication.getPrincipal().getId();
        LOGGER.debug("Received principal id [{}]", uid);
        return sendAuthenticationRequestToSwivel(swivelCredential, uid);
    }

    private HandlerResult sendAuthenticationRequestToSwivel(final SwivelCredential swivelCredential,
                                                            final String uid) throws FailedLoginException {
        if (StringUtils.isBlank(swivelProperties.getSwivelUrl()) || StringUtils.isBlank(swivelProperties.getSharedSecret())) {
            throw new FailedLoginException("Swivel url/shared secret is not specified and cannot be blank.");
        }

        if (StringUtils.isBlank(swivelCredential.getId()) || StringUtils.isBlank(swivelCredential.getToken())) {
            throw new FailedLoginException("Swivel credentials are not specified can cannot be blank");
        }

        /*
         * Create a new session with the Swivel server. We do not support
         * the user having a password on his/her Swivel account, just the
         * one-time code.
         */
        LOGGER.debug("Preparing Swivel request to [{}]", swivelProperties.getSwivelUrl());
        final AgentXmlRequest req = new AgentXmlRequest(swivelProperties.getSwivelUrl(), swivelProperties.getSharedSecret());
        req.setIgnoreSSLErrors(swivelProperties.isIgnoreSslErrors());

        try {
            LOGGER.debug("Submitting Swivel request to [{}] for [{}]", swivelProperties.getSwivelUrl(), uid);
            req.login(uid, StringUtils.EMPTY, swivelCredential.getToken());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        /*
         * Send the request. It will return either PASS (user authenticated)
         * or FAIL (user not authenticated).
         */
        if (!req.send()) {
            LOGGER.error("Swivel request error: [{}], [{}], [{}]", req.getResponseCode(), req.getAgentError(), req.getResponse());
            throw new FailedLoginException("Failed to authenticate swivel token: " + req.getResponse());
        }

        if (req.actionSucceeded()) {
            LOGGER.debug("Successful Swivel authentication for [{}]", uid);
            return createHandlerResult(swivelCredential, this.principalFactory.createPrincipal(uid), null);
        }

        /*
         * A "normal" authentication failure (wrong one-time code)
         * doesn't produce an agent error, so we fake one here to
         * give us something to throw.
         */
        final String agentError = StringUtils.isBlank(req.getAgentError()) ? SWIVEL_ERR_CODE_AUTHN_FAIL : req.getAgentError();
        LOGGER.error("Failed Swivel MFA authentication for [{}] ([{}])", uid, agentError);
        throw new FailedLoginException(ERROR_MAP.getOrDefault(agentError, SWIVEL_ERR_CODE_AUTHN_FAIL));
    }

    @Override
    public boolean supports(final Credential credential) {
        return SwivelCredential.class.isAssignableFrom(credential.getClass());
    }

    private static Map<String, String> createErrorCodeMap() {
        final Map<String, String> errorMap = new HashMap<>();

        errorMap.put("AGENT_ERROR_NO_OTC", "swivel.auth.otc.malformed");
        errorMap.put("AGENT_ERROR_BAD_OTC", "swivel.auth.otc.malformed");

        errorMap.put("AGENT_ERROR_NO_PIN", "swivel.auth.pin.notset");

        errorMap.put("AGENT_ERROR_USER_LOCKED", "swivel.auth.user.locked");
        errorMap.put("AGENT_ERROR_NO_SECURITY_STRINGS", "swivel.auth.user.locked");
        errorMap.put("AGENT_ERROR_AGENT_ACCESS", "swivel.auth.user.notallowed");
        errorMap.put("AGENT_ERROR_USER_NOT_IN_GROUP", "swivel.auth.user.notallowed");
        errorMap.put("AGENT_ERROR_NO_USER_FOUND", "swivel.auth.user.unknown");
        errorMap.put("AGENT_ERROR_NO_AUTH", "swivel.auth.user.unknown");
        errorMap.put("AGENT_ERROR_USERNAME", "swivel.auth.user.unknown");

        errorMap.put("AGENT_ERROR_SESSION", "swivel.server.session.error");
        errorMap.put("AGENT_ERROR_GENERAL", SWIVEL_ERR_CODE_AUTHN_FAIL);

        return errorMap;
    }
}
