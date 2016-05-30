package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * This is {@link YubiKeyApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Resource(name="yubikeyAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Resource(name="yubikeyAuthenticationMetaDataPopulator")
    private YubiKeyAuthenticationMetaDataPopulator populator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(this.populator);
    }
}
