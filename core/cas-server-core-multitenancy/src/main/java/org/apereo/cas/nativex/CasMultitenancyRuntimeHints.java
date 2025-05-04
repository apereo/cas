package org.apereo.cas.nativex;

import org.apereo.cas.multitenancy.TenantAuthenticationPolicy;
import org.apereo.cas.multitenancy.TenantAuthenticationProtocolPolicy;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantDelegatedAuthenticationPolicy;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantUserInterfacePolicy;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link CasMultitenancyRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class CasMultitenancyRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, classLoader, TenantDefinition.class);
        registerReflectionHints(hints, TenantDefinition.class);

        var classes = findSubclassesOf(TenantAuthenticationPolicy.class);
        registerSerializationHints(hints, classLoader, classes);
        registerReflectionHints(hints, classes);

        classes = findSubclassesOf(TenantAuthenticationProtocolPolicy.class);
        registerSerializationHints(hints, classLoader, classes);
        registerReflectionHints(hints, classes);

        classes = findSubclassesOf(TenantUserInterfacePolicy.class);
        registerSerializationHints(hints, classLoader, classes);
        registerReflectionHints(hints, classes);

        classes = findSubclassesOf(TenantDelegatedAuthenticationPolicy.class);
        registerSerializationHints(hints, classLoader, classes);
        registerReflectionHints(hints, classes);

        registerReflectionHints(hints, TenantExtractor.class);
        
        registerProxyHints(hints, TenantsManager.class);
        registerSpringProxyHints(hints, TenantsManager.class, DisposableBean.class);
    }
}
