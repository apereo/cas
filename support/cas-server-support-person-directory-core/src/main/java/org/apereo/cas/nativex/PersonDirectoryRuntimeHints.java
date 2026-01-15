package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.authentication.attribute.AggregatingPersonAttributeDao;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.TenantPrincipalResolver;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This is {@link PersonDirectoryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class PersonDirectoryRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSpringProxyHints(hints, PersonDirectoryAttributeRepositoryPlan.class, DisposableBean.class);
        registerSpringProxyHints(hints, InitializingBean.class, PersonAttributeDao.class);
        registerSpringProxyHints(hints, AggregatingPersonAttributeDao.class, PersonAttributeDao.class);
        registerSpringProxyHints(hints, PrincipalAttributesRepositoryCache.class, Closeable.class);

        registerProxyHints(hints, PersonDirectoryAttributeRepositoryPlanConfigurer.class);

        registerReflectionHints(hints, TenantPrincipalResolver.class);
    }
}

