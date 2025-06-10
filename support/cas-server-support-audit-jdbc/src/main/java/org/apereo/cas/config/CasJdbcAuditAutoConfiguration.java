package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.JdbcAuditTrailEntityFactory;
import org.apereo.cas.audit.generic.JdbcAuditTrailEntity;
import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.audit.AuditJdbcProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaEntityFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.thread.Cleanable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.support.JdbcAuditTrailManager;
import org.apereo.inspektr.audit.support.MaxAgeWhereClauseMatchCriteria;
import org.apereo.inspektr.audit.support.WhereClauseMatchCriteria;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasJdbcAuditAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "jdbc")
@AutoConfiguration
public class CasJdbcAuditAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.audit.jdbc.url").evenIfMissing();

    @Configuration(value = "CasSupportJdbcAuditEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditEntityConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "jpaAuditTrailEntityFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public JpaEntityFactory<AuditTrailEntity> jpaAuditTrailEntityFactory(final CasConfigurationProperties casProperties) {
            val dialect = casProperties.getAudit().getJdbc().getDialect();
            return new JdbcAuditTrailEntityFactory(dialect);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter inspektrAuditJpaVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME) final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @ConditionalOnMissingBean(name = "inspektrAuditEntityManagerFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> inspektrAuditEntityManagerFactory(
            @Qualifier("jpaAuditTrailEntityFactory")
            final JpaEntityFactory<AuditTrailEntity> jpaAuditTrailEntityFactory,
            @Qualifier("inspektrAuditJpaVendorAdapter")
            final JpaVendorAdapter inspektrAuditJpaVendorAdapter,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("inspektrAuditTrailDataSource")
            final DataSource inspektrAuditTrailDataSource,
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return BeanSupplier.of(FactoryBean.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val type = jpaAuditTrailEntityFactory.getType();
                    val ctx = JpaConfigurationContext
                        .builder()
                        .jpaVendorAdapter(inspektrAuditJpaVendorAdapter)
                        .persistenceUnitName("jpaInspektrAuditContext")
                        .dataSource(inspektrAuditTrailDataSource)
                        .packagesToScan(CollectionUtils.wrapSet(type.getPackage().getName()))
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAudit().getJdbc());
                }))
                .otherwiseProxy()
                .get();

        }

        @ConditionalOnMissingBean(name = "auditCleanupCriteria")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WhereClauseMatchCriteria auditCleanupCriteria(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(WhereClauseMatchCriteria.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new MaxAgeWhereClauseMatchCriteria(casProperties.getAudit().getJdbc().getMaxAgeDays()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditTransactionTemplateConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditTransactionTemplateConfiguration {

        @ConditionalOnMissingBean(name = "inspektrAuditTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations inspektrAuditTransactionTemplate(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("inspektrAuditTransactionManager") final PlatformTransactionManager inspektrAuditTransactionManager,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(TransactionOperations.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val template = new TransactionTemplate(inspektrAuditTransactionManager);
                    val jdbc = casProperties.getAudit().getJdbc();
                    template.setIsolationLevelName(jdbc.getIsolationLevelName());
                    template.setPropagationBehaviorName(jdbc.getPropagationBehaviorName());
                    return template;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditManagerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditManagerConfiguration {
        private static String getAuditTableNameFrom(final AuditJdbcProperties jdbc) {
            var tableName = JdbcAuditTrailEntity.AUDIT_TRAIL_TABLE_NAME;
            if (StringUtils.isNotBlank(jdbc.getDefaultSchema())) {
                tableName = jdbc.getDefaultSchema().concat(".").concat(tableName);
            }
            if (StringUtils.isNotBlank(jdbc.getDefaultCatalog())) {
                tableName = jdbc.getDefaultCatalog().concat(".").concat(tableName);
            }
            return tableName;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @DependsOn("inspektrAuditEntityManagerFactory")
        public AuditTrailManager jdbcAuditTrailManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jpaAuditTrailEntityFactory")
            final JpaEntityFactory<AuditTrailEntity> jpaAuditTrailEntityFactory,
            @Qualifier("auditCleanupCriteria")
            final WhereClauseMatchCriteria auditCleanupCriteria,
            @Qualifier("inspektrAuditTransactionTemplate")
            final TransactionOperations inspektrAuditTransactionTemplate,
            @Qualifier("inspektrAuditTrailDataSource")
            final DataSource inspektrAuditTrailDataSource,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuditTrailManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val manager = new JdbcAuditTrailManager(inspektrAuditTransactionTemplate,
                        new JdbcTemplate(inspektrAuditTrailDataSource), jpaAuditTrailEntityFactory);
                    manager.setCleanupCriteria(auditCleanupCriteria);
                    val jdbc = casProperties.getAudit().getJdbc();
                    manager.setAsynchronous(jdbc.isAsynchronous());
                    manager.setColumnLength(jdbc.getColumnLength());
                    manager.setTableName(getAuditTableNameFrom(jdbc));
                    FunctionUtils.doIfNotBlank(jdbc.getSelectSqlQueryTemplate(), manager::setSelectByDateSqlTemplate);
                    FunctionUtils.doIfNotBlank(jdbc.getDateFormatterPattern(), manager::setDateFormatterPattern);
                    FunctionUtils.doIfNotBlank(jdbc.getDateFormatterFunction(), manager::setDateFormatterFunction);
                    return manager;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "jdbcAuditTrailExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailExecutionPlanConfigurer jdbcAuditTrailExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcAuditTrailManager") final AuditTrailManager jdbcAuditTrailManager) {
            return BeanSupplier.of(AuditTrailExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAuditTrailManager(jdbcAuditTrailManager))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditScheduleConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditScheduleConfiguration {

        @ConditionalOnMissingBean(name = "inspektrAuditTrailCleaner")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public Cleanable inspektrAuditTrailCleaner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcAuditTrailManager") final AuditTrailManager jdbcAuditTrailManager) {
            return BeanSupplier.of(Cleanable.class)
                .when(BeanCondition.on("cas.audit.jdbc.schedule.enabled").isTrue().evenIfMissing()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> new JdbcAuditTrailCleaner(jdbcAuditTrailManager))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditTransactionConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "inspektrAuditTransactionManager")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager inspektrAuditTransactionManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("inspektrAuditTrailDataSource") final DataSource inspektrAuditTrailDataSource) {
            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DataSourceTransactionManager(inspektrAuditTrailDataSource))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSupportJdbcAuditDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSupportJdbcAuditDataConfiguration {

        @ConditionalOnMissingBean(name = "inspektrAuditTrailDataSource")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource inspektrAuditTrailDataSource(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getAudit().getJdbc()))
                .otherwiseProxy()
                .get();
        }
    }

    @RequiredArgsConstructor
    static class JdbcAuditTrailCleaner implements Cleanable {
        private final AuditTrailManager jdbcAuditTrailManager;

        @Scheduled(
            cron = "${cas.audit.jdbc.schedule.cron-expression:}",
            zone = "${cas.audit.jdbc.schedule.cron-time-zone:}",
            initialDelayString = "${cas.audit.jdbc.schedule.start-delay:10000}",
            fixedDelayString = "${cas.audit.jdbc.schedule.repeat-interval:30000}")
        @Override
        public void clean() {
            jdbcAuditTrailManager.clean();
        }
    }
}
