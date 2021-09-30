package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.ScriptEnginePersonAttributeDao;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasPersonDirectoryScriptedConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 6.2
 */
@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.script[0]", value = "location")
@Configuration(value = "CasPersonDirectoryScriptedConfiguration", proxyBeanMethods = false)
@Deprecated(since = "6.2.0")
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryScriptedConfiguration {

    @ConditionalOnMissingBean(name = "scriptedAttributeRepositories")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public List<IPersonAttributeDao> scriptedAttributeRepositories(final CasConfigurationProperties casProperties) {
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
        return list;
    }

    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer scriptedPersonDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("scriptedAttributeRepositories")
        final ObjectProvider<List<IPersonAttributeDao>> scriptedAttributeRepositories) {
        return plan -> plan.registerAttributeRepositories(scriptedAttributeRepositories.getObject());
    }

}
