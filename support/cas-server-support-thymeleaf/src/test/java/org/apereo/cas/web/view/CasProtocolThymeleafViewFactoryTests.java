package org.apereo.cas.web.view;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolThymeleafViewFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    ThymeleafAutoConfiguration.class
})
@Tag("Web")
class CasProtocolThymeleafViewFactoryTests {

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val factory = new CasProtocolThymeleafViewFactory(springTemplateEngine, thymeleafProperties);
        val view = (CasThymeleafView) factory.create(applicationContext, "login/casLoginView");
        assertNotNull(view);
        assertNotNull(view.toString());
        assertNotNull(view.getLocale());
    }
}
