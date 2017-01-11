package org.apereo.cas.adaptors.gauth.web.flow;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * This is {@link GoogleAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAccountCheckRegistrationAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAccountCheckRegistrationAction.class);
    
    private final IGoogleAuthenticator googleAuthenticatorInstance;
    private final MultifactorAuthenticationProperties.GAuth gauthProperties;

    public GoogleAccountCheckRegistrationAction(final IGoogleAuthenticator googleAuthenticatorInstance, final MultifactorAuthenticationProperties.GAuth gauth) {
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
        gauthProperties = gauth;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        final String secretKey = googleAuthenticatorInstance.getCredentialRepository().getSecretKey(uid);
        if (StringUtils.isBlank(secretKey)) {
            final GoogleAuthenticatorKey key = this.googleAuthenticatorInstance.createCredentials();
            final GoogleAuthenticatorAccount keyAccount = new GoogleAuthenticatorAccount(uid, key.getKey(),
                    key.getVerificationCode(), key.getScratchCodes());

            final String keyUri = "otpauth://totp/" + gauthProperties.getLabel() + ':' + uid + "?secret="
                    + keyAccount.getSecretKey() + "&issuer=" + gauthProperties.getIssuer();
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("keyUri", keyUri);

            LOGGER.debug("Registration key URI is {}", keyUri);
            
            return new EventFactorySupport().event(this, "register");
        }

        return success();
    }
}
