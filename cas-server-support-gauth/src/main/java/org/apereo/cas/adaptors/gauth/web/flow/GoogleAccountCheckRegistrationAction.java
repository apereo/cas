package org.apereo.cas.adaptors.gauth.web.flow;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
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

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private GoogleAuthenticator googleAuthenticatorInstance;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        if (StringUtils.isNotBlank(googleAuthenticatorInstance.getCredentialRepository().getSecretKey(uid))) {
            final GoogleAuthenticatorKey key = this.googleAuthenticatorInstance.createCredentials();

            final GoogleAuthenticatorAccount keyAccount = new GoogleAuthenticatorAccount(key.getKey(),
                    key.getVerificationCode(), key.getScratchCodes());

            final String keyUri = "otpauth://totp/" + casProperties.getAuthn().getMfa().getGauth().getLabel() + ':' + uid + "?secret="
                    + keyAccount.getSecretKey() + "&issuer=" + casProperties.getAuthn().getMfa().getGauth().getIssuer();
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("keyUri", keyUri);

            return new EventFactorySupport().event(this, "register");
        }

        return success();
    }

    public void setGoogleAuthenticatorInstance(final GoogleAuthenticator googleAuthenticatorInstance) {
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
    }
}
