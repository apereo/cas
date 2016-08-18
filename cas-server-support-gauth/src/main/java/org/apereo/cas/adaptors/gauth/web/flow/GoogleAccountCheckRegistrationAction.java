package org.apereo.cas.adaptors.gauth.web.flow;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    private IGoogleAuthenticator googleAuthenticatorInstance;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        final String secretKey = googleAuthenticatorInstance.getCredentialRepository().getSecretKey(uid);
        if (StringUtils.isBlank(secretKey)) {
            final GoogleAuthenticatorKey key = this.googleAuthenticatorInstance.createCredentials();
            final GoogleAuthenticatorAccount keyAccount = new GoogleAuthenticatorAccount(key.getKey(),
                    key.getVerificationCode(), key.getScratchCodes());

            final String keyUri = "otpauth://totp/" + casProperties.getAuthn().getMfa().getGauth().getLabel() + ':' + uid + "?secret="
                    + keyAccount.getSecretKey() + "&issuer=" + casProperties.getAuthn().getMfa().getGauth().getIssuer();
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("keyUri", keyUri);

            logger.debug("Registration key URI is {}", keyUri);
            
            return new EventFactorySupport().event(this, "register");
        }

        return success();
    }

    public void setGoogleAuthenticatorInstance(final IGoogleAuthenticator googleAuthenticatorInstance) {
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
    }
}
