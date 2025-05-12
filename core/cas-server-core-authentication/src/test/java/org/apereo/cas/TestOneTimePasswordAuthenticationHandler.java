package org.apereo.cas;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.FailedLoginException;

import java.util.Map;

/**
 * Test one-time password authentication handler.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class TestOneTimePasswordAuthenticationHandler extends AbstractAuthenticationHandler {

    private final Map<String, String> credentialMap;
    
    public TestOneTimePasswordAuthenticationHandler(final Map<String, String> credentialMap) {
        super(StringUtils.EMPTY, null, PrincipalFactoryUtils.newPrincipalFactory(), null);
        this.credentialMap = credentialMap;
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential, final Service service) throws Throwable {
        val otp = (OneTimePasswordCredential) credential;
        val valueOnRecord = credentialMap.get(otp.getId());
        if (otp.getPassword().equals(valueOnRecord)) {
            return new DefaultAuthenticationHandlerExecutionResult(this, otp, getPrincipalFactory().createPrincipal(otp.getId()));
        }
        throw new FailedLoginException();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OneTimePasswordCredential;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OneTimePasswordCredential.class.isAssignableFrom(clazz);
    }
}
