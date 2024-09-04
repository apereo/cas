package org.apereo.cas.test;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This is {@link CasTestExtension}. This extension controls global settings
 * that would affect a Spring application context before tests run.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CasTestExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        System.setProperty("spring.mvc.pathmatch.matching-strategy", "ant-path-matcher");
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        System.setProperty("spring.main.banner-mode", "off");
    }
}
