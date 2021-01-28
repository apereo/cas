package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EmailMessageBodyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Mail")
public class EmailMessageBodyBuilderTests {
    @Test
    public void verifyOperation() {
        val props = new EmailProperties().setText("%s, %s");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .parameters(CollectionUtils.wrap("key1", "Hello"))
            .build()
            .addParameter("key2", "World");
        val result = results.produce();
        assertEquals("Hello, World", result);
    }

    @Test
    public void verifyGroovyOperation() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val cacheMgr = new GroovyScriptResourceCacheManager();
        ApplicationContextProvider.registerBeanIntoApplicationContext(appCtx, cacheMgr, ScriptResourceCacheManager.BEAN_NAME);
        
        ApplicationContextProvider.holdApplicationContext(appCtx);
        val props = new EmailProperties().setText("classpath:GroovyMessageBody.groovy");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .parameters(CollectionUtils.wrap("key", "Hello"))
            .build()
            .addParameter("key2", "World");
        val result = results.produce();
        assertEquals("Hello, World", result);
    }
}
