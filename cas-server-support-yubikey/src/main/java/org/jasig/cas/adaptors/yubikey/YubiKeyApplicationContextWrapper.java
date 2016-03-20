package org.jasig.cas.adaptors.yubikey;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link YubiKeyApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class YubiKeyApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("yubikeyAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("yubikeyAuthenticationMetaDataPopulator")
    private YubiKeyAuthenticationMetaDataPopulator populator;

    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(populator);
    }
}
