package org.jasig.cas.adaptors.gauth.web.flow;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.jasig.cas.adaptors.gauth.GoogleAuthenticatorAccount;
import org.jasig.cas.adaptors.gauth.GoogleAuthenticatorAccountRegistry;
import org.jasig.cas.adaptors.gauth.GoogleAuthenticatorInstance;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
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
@RefreshScope
@Component("googleAccountRegistrationAction")
public class GoogleAccountCheckRegistrationAction extends AbstractAction {

    @Value("${cas.mfa.gauth.issuer:CAS}")
    private String issuer;

    @Value("${cas.mfa.gauth.label:CAS}")
    private String label;
    
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private GoogleAuthenticatorAccountRegistry accountRegistry;
    
    @Autowired
    @Qualifier("googleAuthenticatorInstance")
    private GoogleAuthenticatorInstance googleAuthenticatorInstance;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        if (!this.accountRegistry.contains(uid)) {
            final GoogleAuthenticatorKey key = googleAuthenticatorInstance.createCredentials();
            
            final GoogleAuthenticatorAccount keyAccount = new GoogleAuthenticatorAccount(key.getKey(),
                    key.getVerificationCode(), key.getScratchCodes());
            
            final String keyUri = "otpauth://totp/" + this.label + ':' + uid + "?secret=" 
                    + keyAccount.getSecretKey() + "&issuer=" + this.issuer;
            requestContext.getFlowScope().put("key", keyAccount);
            requestContext.getFlowScope().put("keyUri", keyUri);
            
            return new EventFactorySupport().event(this, "register");
        }

        return success();

    }
}
