package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.JsonBackedComplexStubPersonAttributeDao;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasPersonDirectoryJsonConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnMultiValuedProperty(name = "cas.authn.attribute-repository.json[0]", value = "location")
@Configuration(value = "CasPersonDirectoryJsonConfiguration", proxyBeanMethods = false)
@Slf4j
public class CasPersonDirectoryJsonConfiguration {

    @ConditionalOnMissingBean(name = "jsonAttributeRepositories")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public List<IPersonAttributeDao> jsonAttributeRepositories(final CasConfigurationProperties casProperties) {
        val list = new ArrayList<IPersonAttributeDao>();
        casProperties.getAuthn().getAttributeRepository().getJson()
            .stream()
            .filter(json -> ResourceUtils.doesResourceExist(json.getLocation()))
            .forEach(Unchecked.consumer(json -> {
                val r = json.getLocation();
                val dao = new JsonBackedComplexStubPersonAttributeDao(r);
                if (ResourceUtils.isFile(r)) {
                    val watcherService = new FileWatcherService(r.getFile(), Unchecked.consumer(file -> dao.init()));
                    watcherService.start(getClass().getSimpleName());
                    dao.setResourceWatcherService(watcherService);
                }
                dao.setOrder(json.getOrder());
                FunctionUtils.doIfNotNull(json.getId(), dao::setId);
                dao.init();
                LOGGER.debug("Configured JSON attribute sources from [{}]", r);
                list.add(dao);
            }));
        return list;
    }

    @Bean
    @Autowired
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer jsonPersonDirectoryAttributeRepositoryPlanConfigurer(
        @Qualifier("jsonAttributeRepositories") final ObjectProvider<List<IPersonAttributeDao>> jsonAttributeRepositories) {
        return plan -> plan.registerAttributeRepositories(jsonAttributeRepositories.getObject());
    }
}
