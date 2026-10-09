package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.services.web.CasThymeleafTemplatesDirector;
import org.apereo.cas.web.view.CasMustacheView;
import org.apereo.cas.web.view.CasThymeleafView;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DecoratingProxy;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasThymeleafRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasThymeleafRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasThymeleafRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(View.class, BeanNameAware.class,
            ServletContextAware.class, ApplicationContextAware.class, SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forBundle("messages").test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasThymeleafTemplatesDirector.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasThymeleafView.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(CasMustacheView.class).test(hints));
    }
}
