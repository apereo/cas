package org.apereo.cas.web.view;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolThymeleafViewFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = ThymeleafAutoConfiguration.class)
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class CasProtocolThymeleafViewFactoryTests {

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val factory = new CasProtocolThymeleafViewFactory(springTemplateEngine, thymeleafProperties);
        val view = (CasThymeleafView) factory.create(applicationContext, "login/casLoginView");
        assertNotNull(view);
        assertNotNull(view.toString());
        assertNotNull(view.getLocale());
    }
}
