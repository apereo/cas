package org.apereo.cas.config;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlConfigOperations;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import com.github.dbadia.sqrl.server.data.SqrlCorrelator;
import com.github.dbadia.sqrl.server.data.SqrlIdentity;
import com.github.dbadia.sqrl.server.data.SqrlJpaPersistenceProvider;
import com.github.dbadia.sqrl.server.data.SqrlUsedNutToken;
import com.github.dbadia.sqrl.server.util.SqrlServiceExecutor;
import com.zaxxer.hikari.hibernate.HikariConnectionProvider;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.sqrl.SqrlAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.sqrl.SqrlCallbabckController;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SqrlGenerateQRAction;
import org.apereo.cas.web.flow.SqrlWebflowConfigurer;
import org.hibernate.cfg.Environment;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.quartz.utils.HikariCpPoolingConnectionProvider;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This is {@link SqrlConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("SqrlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class SqrlConfiguration {
    private static final int AES_KEY_SIZE = 16;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaSqrlVendorAdapter() {
        return Beans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @ConditionalOnMissingBean(name = "sqrlConfig")
    @Bean
    @RefreshScope
    public SqrlConfig sqrlConfig() {
        try {
            final SqrlConfig c = new SqrlConfig();
            final byte[] key = DigestUtils.sha("thekey".getBytes(StandardCharsets.UTF_8));
            c.setAESKeyBytes(Arrays.copyOf(key, AES_KEY_SIZE));
            c.setBackchannelServletPath("/cas/sqrlcallback");
            c.setSecureRandom(new SecureRandom());
            c.setServerFriendlyName(casProperties.getServer().getPrefix());
            c.setCookieDomain(casProperties.getTgc().getDomain());
            return c;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public CasWebflowConfigurer sqrlWebflowConfigurer() {
        return new SqrlWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sqrlGenerateQRAction")
    public Action sqrlGenerateQRAction() {
        return new SqrlGenerateQRAction(sqrlConfig(), sqrlServerOperations());
    }

    @ConditionalOnMissingBean(name = "sqrlServerOperations")
    @Bean
    @RefreshScope
    public SqrlServerOperations sqrlServerOperations() {
        SqrlServerOperations.setExecutor(new SqrlServiceExecutor());
        SqrlConfigOperations.setExecutor(new SqrlServiceExecutor());
        return new SqrlServerOperations(sqrlConfig());
    }

    @Bean
    @ConditionalOnMissingBean(name = "sqrlCallbackController")
    @RefreshScope
    public SqrlCallbabckController sqrlCallbackController() {
        return new SqrlCallbabckController(sqrlConfig(), sqrlServerOperations());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean sqrlEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setJpaVendorAdapter(jpaSqrlVendorAdapter());
        bean.setPersistenceUnitName(SqrlJpaPersistenceProvider.PERSISTENCE_UNIT_NAME);
        bean.setPersistenceUnitPostProcessors(new SqrlPersistenceUnitPostProcessor());
        bean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        bean.setJpaDialect(new HibernateJpaDialect());
        return bean;
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerSqrl(@Qualifier("sqrlEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    /**
     * The Sqrl persistence unit post processor to inject settings into the persistence config file
     * required by the sqrl server library.
     */
    public class SqrlPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

        @Override
        public void postProcessPersistenceUnitInfo(final MutablePersistenceUnitInfo unit) {
            final SqrlAuthenticationProperties.Jpa jpa = casProperties.getAuthn().getSqrl().getJpa();

            unit.addProperty(Environment.SHOW_SQL, String.valueOf(casProperties.getJdbc().isShowSql()));
            unit.addProperty(Environment.FORMAT_SQL, Boolean.TRUE.toString());
            unit.addProperty(Environment.HBM2DDL_AUTO, jpa.getDdlAuto());
            unit.addProperty(Environment.DEFAULT_BATCH_FETCH_SIZE, String.valueOf(jpa.getBatchSize()));
            unit.addProperty(Environment.POOL_SIZE, String.valueOf(jpa.getPool().getMaxSize()));
            unit.addProperty(Environment.URL, StringUtils.defaultIfBlank(jpa.getUrl(), StringUtils.EMPTY));
            unit.addProperty(Environment.PASS, StringUtils.defaultIfBlank(jpa.getPassword(), StringUtils.EMPTY));
            unit.addProperty(Environment.USER, StringUtils.defaultIfBlank(jpa.getUser(), StringUtils.EMPTY));
            unit.addProperty(Environment.DRIVER, StringUtils.defaultIfBlank(jpa.getDriverClass(), StringUtils.EMPTY));
            unit.addProperty(Environment.DIALECT, jpa.getDialect());

            if (StringUtils.isNotBlank(jpa.getDataSourceName())) {
                unit.addProperty(Environment.DATASOURCE, jpa.getDataSourceName());
            }
            if (StringUtils.isNotBlank(jpa.getDefaultSchema())) {
                unit.addProperty(Environment.DEFAULT_SCHEMA, jpa.getDefaultSchema());
            }
            if (StringUtils.isNotBlank(jpa.getDefaultCatalog())) {
                unit.addProperty(Environment.DEFAULT_CATALOG, jpa.getDefaultCatalog());
            }

            unit.addProperty(Environment.CONNECTION_PROVIDER, HikariCPConnectionProvider.class.getName());
            unit.addProperty("hibernate.hikari.idleTimeout", String.valueOf(jpa.getIdleTimeout()));
            unit.addProperty("hibernate.hikari.maximumPoolSize", String.valueOf(jpa.getPool().getMaxSize()));
            unit.addProperty("hibernate.hikari.minimumIdle", String.valueOf(jpa.getPool().getMinSize()));

            unit.setPersistenceProviderClassName(HibernatePersistenceProvider.class.getName());

            unit.addManagedClassName(SqrlIdentity.class.getName());
            unit.addManagedClassName(SqrlCorrelator.class.getName());
            unit.addManagedClassName(SqrlUsedNutToken.class.getName());

            unit.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
            unit.setPersistenceUnitName(SqrlJpaPersistenceProvider.PERSISTENCE_UNIT_NAME);
        }
    }

}
