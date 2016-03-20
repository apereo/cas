package org.jasig.cas.adaptors.duo;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.web.AbstractServletContextInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;

/**
 * This is {@link DuoServletContextListener}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@WebListener
@Component
public class DuoServletContextListener extends AbstractServletContextInitializer {
    @Autowired
    @Qualifier("duoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("duoAuthenticationMetaDataPopulator")
    private AuthenticationMetaDataPopulator populator;

    @Override
    protected void initializeRootApplicationContext() {
        addAuthenticationHandler(this.authenticationHandler);
        addAuthenticationMetadataPopulator(populator);
    }
}
