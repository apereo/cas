package org.apereo.cas.config;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlConfigOperations;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import com.github.dbadia.sqrl.server.data.SqrlJpaPersistenceProvider;
import com.github.dbadia.sqrl.server.util.SqrlServiceExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.sqrl.SqrlAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.sqrl.SqrlCallbabckController;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SqrlGenerateQRAction;
import org.apereo.cas.web.flow.SqrlWebflowConfigurer;
import org.hibernate.jpa.HibernatePersistenceProvider;
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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.persistence.EntityManagerFactory;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;

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
        public void postProcessPersistenceUnitInfo(final MutablePersistenceUnitInfo mutablePersistenceUnitInfo) {
            final Properties props = new Properties();
            final SqrlAuthenticationProperties.Jpa jpa = casProperties.getAuthn().getSqrl().getJpa();

            props.put("schema", jpa.getDefaultSchema());
            props.put("catalog", jpa.getDefaultCatalog());
            props.put("batch", jpa.getBatchSize());
            props.put("pool", jpa.getPool().getMaxSize());
            props.put("driver", jpa.getDriverClass());
            props.put("user", jpa.getUser());
            props.put("password", jpa.getPassword());
            props.put("url", jpa.getUrl());
            props.put("dialect", jpa.getDialect());
            props.put("ddl", jpa.getDdlAuto());
            props.put("showSql", casProperties.getJdbc().isShowSql());
            
            mutablePersistenceUnitInfo.setProperties(props);
        }
    }

}
