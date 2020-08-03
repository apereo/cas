package org.apereo.cas.web.flow;

import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.web.flow.config.OpenIdWebflowConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@Tag("Webflow")
@SpringBootTest(classes = {
    AbstractOpenIdTests.SharedTestConfiguration.class,
    OpenIdWebflowConfiguration.class
})
@Deprecated(since = "6.2.0")
public class DefaultOpenIdUserNameExtractorTests {

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
