package org.jasig.cas.web;

import org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is {@link ClearpassApplicationContextWrapper} that auto configures
 * the application context for capturing and caching of the credentials.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class ClearpassApplicationContextWrapper extends BaseApplicationContextWrapper {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${cas.clearpass.cache.credential:false}")
    private boolean cacheCredential;
    
    /**
     * Initialize application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (this.cacheCredential) {
            logger.info("Credential caching is enabled");
            addAuthenticationMetadataPopulator(new CacheCredentialsMetaDataPopulator());
        }
    }
}
