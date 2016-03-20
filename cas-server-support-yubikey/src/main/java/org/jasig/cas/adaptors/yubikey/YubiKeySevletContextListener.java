package org.jasig.cas.adaptors.yubikey;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * This is {@link YubiKeySevletContextListener}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@WebListener
@Component
public class YubiKeySevletContextListener extends AbstractServletContextInitializer {
    @Autowired
    @Qualifier("yubikeyAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("yubikeyAuthenticationMetaDataPopulator")
    private YubiKeyAuthenticationMetaDataPopulator populator;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(populator);
    }
}
