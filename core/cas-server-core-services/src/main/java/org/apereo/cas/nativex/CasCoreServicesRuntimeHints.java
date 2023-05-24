package org.apereo.cas.nativex;

import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyEnforcer;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServiceRegistryInitializerEventListener;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

/**
 * This is {@link CasCoreServicesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreServicesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(ServiceRegistryInitializer.class)
            .registerJdkProxy(RegisteredServiceAccessStrategyEnforcer.class)
            .registerJdkProxy(ServiceRegistry.class)
            .registerJdkProxy(ServiceRegistryExecutionPlanConfigurer.class);

        registerSpringProxy(hints, ServiceRegistryInitializerEventListener.class);

        List.of(CasRegisteredService.class)
            .forEach(el ->
                hints.reflection().registerType(TypeReference.of(el),
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.DECLARED_FIELDS,
                    MemberCategory.PUBLIC_FIELDS));
    }
}
