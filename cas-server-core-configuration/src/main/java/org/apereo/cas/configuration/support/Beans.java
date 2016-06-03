package org.apereo.cas.configuration.support;

import com.zaxxer.hikari.HikariDataSource;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * <code>@Bean creation methods</code> throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class Beans {

    //non-instatiable
    private Beans() {
    }

    public static HikariDataSource newHickariDataSource(final AbstractJpaProperties jpaProperties) {
        try {
            final HikariDataSource bean = new HikariDataSource();
            bean.setDriverClassName(jpaProperties.getDriverClass());
            bean.setJdbcUrl(jpaProperties.getUrl());
            bean.setUsername(jpaProperties.getUser());
            bean.setPassword(jpaProperties.getPassword());

            bean.setMaximumPoolSize(jpaProperties.getPool().getMaxSize());
            bean.setMinimumIdle(jpaProperties.getPool().getMaxIdleTime());
            bean.setIdleTimeout(jpaProperties.getIdleTimeout());
            bean.setLeakDetectionThreshold(jpaProperties.getLeakThreshold());
            bean.setInitializationFailFast(jpaProperties.isFailFast());
            bean.setIsolateInternalQueries(jpaProperties.isolateInternalQueries());
            bean.setConnectionTestQuery(jpaProperties.getHealthQuery());
            bean.setAllowPoolSuspension(jpaProperties.getPool().isSuspension());
            bean.setAutoCommit(jpaProperties.isAutocommit());
            bean.setLoginTimeout(jpaProperties.getPool().getMaxWait());
            bean.setValidationTimeout(jpaProperties.getPool().getMaxWait());
            return bean;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
