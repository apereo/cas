package org.apereo.cas.web;

import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * This is {@link ClearpassApplicationContextWrapper} that auto configures
 * the application context for capturing and caching of the credentials.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ClearpassApplicationContextWrapper extends BaseApplicationContextWrapper {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private boolean cacheCredential;

    public void setCacheCredential(final boolean cacheCredential) {
        this.cacheCredential = cacheCredential;
    }

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
