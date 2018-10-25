package org.apereo.cas.support.openid.web.support;

import org.apereo.cas.support.openid.AbstractOpenIdTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class DefaultOpenIdUserNameExtractorTests extends AbstractOpenIdTests {

    @Autowired
    @Qualifier("defaultOpenIdUserNameExtractor")
    private OpenIdUserNameExtractor extractor;

    @Test
    public void verifyExtractionSuccessful() {
        assertEquals("scootman28", this.extractor.extractLocalUsernameFromUri("http://test.com/scootman28"));
    }

    @Test
    public void verifyExtractionFailed() {
        assertNull(this.extractor.extractLocalUsernameFromUri("test.com"));
    }

    @Test
    public void verifyNull() {
        assertNull(this.extractor.extractLocalUsernameFromUri(null));
    }
}
