package org.apereo.cas.adaptors.duo.authn.web;

import com.duosecurity.duoweb.DuoWeb;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.authn.BaseDuoAuthenticationService;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public class DuoWebAuthenticationService extends BaseDuoAuthenticationService<String> {


    /**
     * Creates the duo authentication service.
     *
     * @param duoProperties
     */
    public DuoWebAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties) {
        super(duoProperties);
    }


    @Override
    public String signRequestToken(final String uid) {
        return DuoWeb.signRequest(duoProperties.getDuoIntegrationKey(),
                duoProperties.getDuoSecretKey(),
                duoProperties.getDuoApplicationKey(), uid);
    }

    @Override
    public String authenticate(final Credential creds) throws Exception {
        final String signedRequestToken = DuoCredential.class.cast(creds).getSignedDuoResponse();
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }

        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(duoProperties.getDuoIntegrationKey(),
                duoProperties.getDuoSecretKey(),
                duoProperties.getDuoApplicationKey(), signedRequestToken);
    }



}
