package org.apereo.cas.web.view;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolThymeleafViewFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ThymeleafAutoConfiguration.class
})
@Tag("Simple")
public class CasProtocolThymeleafViewFactoryTests {

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        val factory = new CasProtocolThymeleafViewFactory(springTemplateEngine, thymeleafProperties);
        val view = factory.create(applicationContext, "casLoginView");
        assertNotNull(view);
        assertNotNull(view.toString());
    }
}
