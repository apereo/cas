package org.apereo.cas.util.spring;

import org.apereo.cas.util.spring.beans.BeanContainer;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ApplicationContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
class ApplicationContextProviderTests {
    @Test
    void verifyOperation() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val container = BeanContainer.of("Hello", "World");
        appCtx.registerBean("beanContainer", BeanContainer.class);
        appCtx.getBeanFactory().initializeBean(container, "beanContainer");
        appCtx.getBeanFactory().autowireBean(container);
        appCtx.getBeanFactory().registerSingleton("beanContainer", container);

        ApplicationContextProvider.holdApplicationContext(appCtx);
        assertDoesNotThrow(() -> {
            val holding = new HoldingBeanContainer();
            ApplicationContextProvider.processBeanInjections(holding);
            assertNotNull(holding.getContainer());
            assertEquals(2, holding.getContainer().size());
        });
    }

    @Getter
    private static final class HoldingBeanContainer {
        @Autowired
        @Qualifier("beanContainer")
        private BeanContainer<String> container;
    }
}
