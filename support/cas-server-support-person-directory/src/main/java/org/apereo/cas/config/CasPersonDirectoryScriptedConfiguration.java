package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ScriptEnginePersonAttributeDao;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * This is {@link CasPersonDirectoryScriptedConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 6.2
 */
@Deprecated(since = "6.2.0")
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasPersonDirectoryScriptedConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.attribute-repository.script[0].location");

    @Configuration(value = "ScriptAttributeRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class ScriptAttributeRepositoryConfiguration {
        @ConditionalOnMissingBean(name = "scriptedAttributeRepositories")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<IPersonAttributeDao> scriptedAttributeRepositories(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val list = new ArrayList<IPersonAttributeDao>();
                    casProperties.getAuthn().getAttributeRepository().getScript()
                        .forEach(Unchecked.consumer(script -> {
                            val scriptContents = IOUtils.toString(script.getLocation().getInputStream(), StandardCharsets.UTF_8);
                            val engineName = script.getEngineName() == null
                                ? ScriptEnginePersonAttributeDao.getScriptEngineName(script.getLocation().getFilename())
                                : script.getEngineName();
                            val dao = new ScriptEnginePersonAttributeDao(scriptContents, engineName);
                            dao.setCaseInsensitiveUsername(script.isCaseInsensitive());
                            dao.setOrder(script.getOrder());
                            FunctionUtils.doIfNotNull(script.getId(), dao::setId);
                            LOGGER.debug("Configured scripted attribute sources from [{}]", script.getLocation());
                            list.add(dao);
                        }));
                    return BeanContainer.of(list);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }
    }

    @Configuration(value = "ScriptAttributeRepositoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class ScriptAttributeRepositoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "scriptedPersonDirectoryAttributeRepositoryPlanConfigurer")
        public PersonDirectoryAttributeRepositoryPlanConfigurer scriptedPersonDirectoryAttributeRepositoryPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("scriptedAttributeRepositories")
            final BeanContainer<IPersonAttributeDao> scriptedAttributeRepositories) {
            return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAttributeRepositories(scriptedAttributeRepositories.toList()))
                .otherwiseProxy()
                .get();
        }
    }

}
