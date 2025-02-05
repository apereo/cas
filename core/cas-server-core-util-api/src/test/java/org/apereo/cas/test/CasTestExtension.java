package org.apereo.cas.test;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;

/**
 * This is {@link CasTestExtension}. This extension controls global settings
 * that would affect a Spring application context before tests run.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CasTestExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(final ExtensionContext extensionContext) throws Exception {
        System.setProperty("spring.mvc.pathmatch.matching-strategy", WebMvcProperties.MatchingStrategy.ANT_PATH_MATCHER.name());
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        System.setProperty("spring.main.banner-mode", "off");

        val requestContext = MockRequestContext.create();
        requestContext
            .setRemoteAddr("127.26.152.11")
            .setLocalAddr("109.98.51.12")
            .withUserAgent()
            .setClientInfo();
    }
}
