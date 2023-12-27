package org.apereo.cas.nativex;

import org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.hibernate.dialect.Dialect;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.orm.jpa.JpaVendorAdapter;
import java.util.List;

/**
 * This is {@link CasHibernateRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasHibernateRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerReflectionHints(hints, findSubclassesInPackage(Dialect.class, "org.hibernate"));
        registerReflectionHints(hints, List.of(JpaVendorAdapter.class, CasHibernatePhysicalNamingStrategy.class));
    }

}
