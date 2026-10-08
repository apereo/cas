package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ResourceBasedServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManagerScheduledLoader;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.DecoratingProxy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreServicesRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasCoreServicesRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasCoreServicesRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(ServicesManagerScheduledLoader.class.getDeclaredMethod("run")).test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forResource("services/Simple-12345.json").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forResource("services/native/Simple-12345.json").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forResource("services/.donotdel").test(hints));
        assertTrue(RuntimeHintsPredicates.resource().forResource("services/Simple-12345.json.ignore").test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ServiceRegistryInitializer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ServiceRegistry.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(ResourceBasedServiceRegistry.class,
            DisposableBean.class, ServiceRegistry.class, SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));

        assertTrue(RuntimeHintsPredicates.reflection().onType(CasRegisteredService.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(BaseRegisteredService.class).test(hints));
        val serviceHint = hints.reflection().getTypeHint(SimpleWebApplicationServiceImpl.class);
        assertNotNull(serviceHint);
        assertTrue(serviceHint.hasJavaSerialization());
    }
}
