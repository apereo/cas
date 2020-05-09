package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.persondir.DefaultPersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.CascadingPersonAttributeDao;
import org.apereo.services.persondir.support.GroovyPersonAttributeDao;
import org.apereo.services.persondir.support.GrouperPersonAttributeDao;
import org.apereo.services.persondir.support.JsonBackedComplexStubPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.RestfulPersonAttributeDao;
import org.apereo.services.persondir.support.ScriptEnginePersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.MultiRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpMethod;

import javax.naming.directory.SearchControls;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link CasPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casPersonDirectoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasPersonDirectoryConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<List<PersonDirectoryAttributeRepositoryPlanConfigurer>> attributeRepositoryConfigurers;
    
    @ConditionalOnMissingBean(name = "attributeDefinitionStore")
    @Bean
    @RefreshScope
    public AttributeDefinitionStore attributeDefinitionStore() throws Exception {
        val resource = casProperties.getPersonDirectory().getAttributeDefinitionStore().getJson().getLocation();
        val store = new DefaultAttributeDefinitionStore(resource);
        store.setScope(casProperties.getServer().getScope());
        return store;
    }

    @ConditionalOnMissingBean(name = "attributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> attributeRepositories() {
        val configurers = (List<PersonDirectoryAttributeRepositoryPlanConfigurer>)
            ObjectUtils.defaultIfNull(attributeRepositoryConfigurers.getIfAvailable(), new ArrayList<>(0));
        val plan = new DefaultPersonDirectoryAttributeRepositoryPlan();
        configurers.forEach(c -> c.configureAttributeRepositoryPlan(plan));
        val list = new ArrayList<IPersonAttributeDao>(plan.getAttributeRepositories());
        list.addAll(stubAttributeRepositories());
        AnnotationAwareOrderComparator.sort(list);
        LOGGER.trace("Final list of attribute repositories is [{}]", list);
        return list;
    }

    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean
    @RefreshScope
    public IPersonAttributeDao attributeRepository() {
        return cachingAttributeRepository();
    }

    @ConditionalOnMissingBean(name = "stubAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> stubAttributeRepositories() {
        val list = new ArrayList<IPersonAttributeDao>();
        val stub = casProperties.getAuthn().getAttributeRepository().getStub();
        val attrs = stub.getAttributes();
        if (!attrs.isEmpty()) {
            LOGGER.info("Found and added static attributes [{}] to the list of candidate attribute repositories", attrs.keySet());
            val dao = Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
            list.add(dao);
        }
        return list;
    }

    @Bean
    @ConditionalOnMissingBean(name = "cachingAttributeRepository")
    @RefreshScope
    public IPersonAttributeDao cachingAttributeRepository() {
        val props = casProperties.getAuthn().getAttributeRepository();
        if (props.getExpirationTime() <= 0) {
            LOGGER.warn("Attribute repository caching is disabled");
            return aggregatingAttributeRepository();
        }

        val impl = new CachingPersonAttributeDaoImpl();
        impl.setCacheNullResults(false);
        val graphs = Caffeine.newBuilder()
            .maximumSize(props.getMaximumCacheSize())
            .expireAfterWrite(props.getExpirationTime(), TimeUnit.valueOf(props.getExpirationTimeUnit().toUpperCase()))
            .build();
        impl.setUserInfoCache((Map) graphs.asMap());
        impl.setCachedPersonAttributesDao(aggregatingAttributeRepository());
        LOGGER.trace("Configured cache expiration policy for merging attribute sources to be [{}] minute(s)", props.getExpirationTime());
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean(name = "aggregatingAttributeRepository")
    @RefreshScope
    public IPersonAttributeDao aggregatingAttributeRepository() {
        val aggregate = getAggregateAttributeRepository();

        val properties = casProperties.getAuthn().getAttributeRepository();
        val merger = StringUtils.defaultIfBlank(properties.getMerger(), "replace").trim();
        LOGGER.trace("Configured merging strategy for attribute sources is [{}]", merger);
        aggregate.setMerger(CoreAuthenticationUtils.getAttributeMerger(merger));

        val list = attributeRepositories();
        aggregate.setPersonAttributeDaos(list);

        if (list.isEmpty()) {
            LOGGER.debug("No attribute repository sources are available/defined to merge together.");
        } else {
            val names = list
                .stream()
                .map(p -> Arrays.toString(p.getId()))
                .collect(Collectors.joining(","));
            LOGGER.debug("Configured attribute repository sources to merge together: [{}]", names);
        }
        return aggregate;
    }

    private AbstractAggregatingDefaultQueryPersonAttributeDao getAggregateAttributeRepository() {
        val properties = casProperties.getAuthn().getAttributeRepository();
        val aggregation = StringUtils.defaultIfBlank(properties.getAggregation(), "merge").trim().toLowerCase();
        switch (aggregation) {
            case "cascade":
            case "query":
                val dao = new CascadingPersonAttributeDao();
                dao.setAddOriginalAttributesToQuery(true);
                dao.setStopIfFirstDaoReturnsNull(true);
                return dao;
            case "merge":
            case "combine":
            default:
                return new MergingPersonAttributeDaoImpl();
        }
    }

    @ConditionalOnClass(value = JpaBeans.class)
    @ConditionalOnProperty(name = "cas.authn.attribute-repository.jdbc[0].sql")
    @Configuration("CasPersonDirectoryJdbcConfiguration")
    public class CasPersonDirectoryJdbcConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        private AbstractJdbcPersonAttributeDao createJdbcPersonAttributeDao(final JdbcPrincipalAttributesProperties jdbc) {
            if (jdbc.isSingleRow()) {
                LOGGER.debug("Configured single-row JDBC attribute repository for [{}]", jdbc.getUrl());
                return new SingleRowJdbcPersonAttributeDao(
                    JpaBeans.newDataSource(jdbc),
                    jdbc.getSql()
                );
            }
            LOGGER.debug("Configured multi-row JDBC attribute repository for [{}]", jdbc.getUrl());
            val jdbcDao = new MultiRowJdbcPersonAttributeDao(
                JpaBeans.newDataSource(jdbc),
                jdbc.getSql()
            );
            LOGGER.debug("Configured multi-row JDBC column mappings for [{}] are [{}]", jdbc.getUrl(), jdbc.getColumnMappings());
            jdbcDao.setNameValueColumnMappings(jdbc.getColumnMappings());
            return jdbcDao;
        }
        
        @ConditionalOnMissingBean(name = "jdbcAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> jdbcAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            val attrs = casProperties.getAuthn().getAttributeRepository();
            attrs.getJdbc()
                .stream()
                .filter(jdbc -> StringUtils.isNotBlank(jdbc.getSql()) && StringUtils.isNotBlank(jdbc.getUrl()))
                .forEach(jdbc -> {
                    val jdbcDao = createJdbcPersonAttributeDao(jdbc);
                    FunctionUtils.doIfNotNull(jdbc.getId(), jdbcDao::setId);

                    jdbcDao.setQueryAttributeMapping(CollectionUtils.wrap("username", jdbc.getUsername()));
                    val mapping = jdbc.getAttributes();
                    if (mapping != null && !mapping.isEmpty()) {
                        LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", jdbc.getUrl(), jdbc.getAttributes());
                        jdbcDao.setResultAttributeMapping(mapping);
                    }
                    jdbcDao.setRequireAllQueryAttributes(jdbc.isRequireAllAttributes());
                    jdbcDao.setUsernameCaseCanonicalizationMode(jdbc.getCaseCanonicalization());
                    jdbcDao.setDefaultCaseCanonicalizationMode(jdbc.getCaseCanonicalization());
                    jdbcDao.setQueryType(jdbc.getQueryType());
                    jdbcDao.setOrder(jdbc.getOrder());
                    list.add(jdbcDao);
                });
            return list;
        }


        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(jdbcAttributeRepositories());
        }
    }

    @ConditionalOnProperty(name = "cas.authn.attribute-repository.ldap[0].ldap-url")
    @Configuration("CasPersonDirectoryLdapConfiguration")
    public class CasPersonDirectoryLdapConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @ConditionalOnMissingBean(name = "ldapAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> ldapAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            val attrs = casProperties.getAuthn().getAttributeRepository();
            attrs.getLdap()
                .stream()
                .filter(ldap -> StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getLdapUrl()))
                .forEach(ldap -> {
                    val ldapDao = new LdaptivePersonAttributeDao();
                    FunctionUtils.doIfNotNull(ldap.getId(), ldapDao::setId);
                    LOGGER.debug("Configured LDAP attribute source for [{}] and baseDn [{}]", ldap.getLdapUrl(), ldap.getBaseDn());
                    ldapDao.setConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap));
                    ldapDao.setBaseDN(ldap.getBaseDn());

                    LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(), ldap.getSearchFilter());
                    ldapDao.setSearchFilter(ldap.getSearchFilter());

                    val constraints = new SearchControls();
                    if (ldap.getAttributes() != null && !ldap.getAttributes().isEmpty()) {
                        LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", ldap.getLdapUrl(), ldap.getAttributes());
                        ldapDao.setResultAttributeMapping(ldap.getAttributes());
                        val attributes = (String[]) ldap.getAttributes().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
                        constraints.setReturningAttributes(attributes);
                    } else {
                        LOGGER.debug("Retrieving all attributes as no explicit attribute mappings are defined for [{}]", ldap.getLdapUrl());
                        constraints.setReturningAttributes(null);
                    }

                    if (ldap.isSubtreeSearch()) {
                        LOGGER.debug("Configured subtree searching for [{}]", ldap.getLdapUrl());
                        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    }
                    constraints.setDerefLinkFlag(true);
                    ldapDao.setSearchControls(constraints);
                    ldapDao.setOrder(ldap.getOrder());
                    LOGGER.debug("Adding LDAP attribute source for [{}]", ldap.getLdapUrl());
                    list.add(ldapDao);
                });

            return list;
        }


        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(ldapAttributeRepositories());
        }
    }

    @ConditionalOnProperty(name = "cas.authn.attribute-repository.rest[0].url")
    @Configuration("CasPersonDirectoryRestConfiguration")
    public class CasPersonDirectoryRestConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {
        @ConditionalOnMissingBean(name = "restfulAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> restfulAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            casProperties.getAuthn().getAttributeRepository().getRest()
                .stream()
                .filter(rest -> StringUtils.isNotBlank(rest.getUrl()))
                .forEach(rest -> {
                    val dao = new RestfulPersonAttributeDao();
                    dao.setCaseInsensitiveUsername(rest.isCaseInsensitive());
                    dao.setOrder(rest.getOrder());
                    FunctionUtils.doIfNotNull(rest.getId(), dao::setId);
                    dao.setUrl(rest.getUrl());
                    dao.setMethod(Objects.requireNonNull(HttpMethod.resolve(rest.getMethod())).name());

                    if (StringUtils.isNotBlank(rest.getBasicAuthPassword()) && StringUtils.isNotBlank(rest.getBasicAuthUsername())) {
                        dao.setBasicAuthPassword(rest.getBasicAuthPassword());
                        dao.setBasicAuthUsername(rest.getBasicAuthUsername());
                        LOGGER.debug("Basic authentication credentials are located for REST endpoint [{}]", rest.getUrl());
                    } else {
                        LOGGER.debug("Basic authentication credentials are not defined for REST endpoint [{}]", rest.getUrl());
                    }

                    LOGGER.debug("Configured REST attribute sources from [{}]", rest.getUrl());
                    list.add(dao);
                });

            return list;
        }

        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(restfulAttributeRepositories());
        }
    }

    @ConditionalOnProperty(name = "cas.authn.attribute-repository.groovy[0].location")
    @Configuration("CasPersonDirectoryGroovyConfiguration")
    public class CasPersonDirectoryGroovyConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @ConditionalOnMissingBean(name = "groovyAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> groovyAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            casProperties.getAuthn().getAttributeRepository().getGroovy()
                .stream()
                .filter(groovy -> groovy.getLocation() != null)
                .forEach(groovy -> {
                    val dao = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties));
                    dao.setCaseInsensitiveUsername(groovy.isCaseInsensitive());
                    dao.setOrder(groovy.getOrder());
                    FunctionUtils.doIfNotNull(groovy.getId(), dao::setId);
                    LOGGER.debug("Configured Groovy attribute sources from [{}]", groovy.getLocation());
                    list.add(dao);
                });
            return list;
        }

        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(groovyAttributeRepositories());
        }
    }

    @ConditionalOnProperty(name = "cas.authn.attribute-repository.script[0].location")
    @Configuration("CasPersonDirectoryScriptedConfiguration")
    @Deprecated(since = "6.2.0")
    public class CasPersonDirectoryScriptedConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @ConditionalOnMissingBean(name = "scriptedAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> scriptedAttributeRepositories() {
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

        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(scriptedAttributeRepositories());
        }
    }

    @ConditionalOnProperty(name = "cas.authn.attribute-repository.json[0].location")
    @Configuration("CasPersonDirectoryRestConfiguration")
    public class CasPersonDirectoryJsonConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @ConditionalOnMissingBean(name = "jsonAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> jsonAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            casProperties.getAuthn().getAttributeRepository().getJson()
                .stream()
                .filter(json -> ResourceUtils.doesResourceExist(json.getLocation()))
                .forEach(Unchecked.consumer(json -> {
                    val r = json.getLocation();
                    val dao = new JsonBackedComplexStubPersonAttributeDao(r);
                    try {
                        if (r.isFile()) {
                            val watcherService = new FileWatcherService(r.getFile(), file -> {
                                try {
                                    dao.init();
                                } catch (final Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            });
                            watcherService.start(getClass().getSimpleName());
                            dao.setResourceWatcherService(watcherService);
                        }
                    } catch (final Exception e) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                    dao.setOrder(json.getOrder());
                    FunctionUtils.doIfNotNull(json.getId(), dao::setId);
                    dao.init();
                    LOGGER.debug("Configured JSON attribute sources from [{}]", r);
                    list.add(dao);
                }));
            return list;
        }


        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(jsonAttributeRepositories());
        }
    }

    @ConditionalOnProperty(prefix = "cas.authn.attribute-repository.grouper", name = "enabled", havingValue = "true")
    @Configuration("CasPersonDirectoryGrouperConfiguration")
    public class CasPersonDirectoryGrouperConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @ConditionalOnMissingBean(name = "grouperAttributeRepositories")
        @Bean
        @RefreshScope
        public List<IPersonAttributeDao> grouperAttributeRepositories() {
            val list = new ArrayList<IPersonAttributeDao>();
            val gp = casProperties.getAuthn().getAttributeRepository().getGrouper();

            if (gp.isEnabled()) {
                val dao = new GrouperPersonAttributeDao();
                dao.setOrder(gp.getOrder());
                FunctionUtils.doIfNotNull(gp.getId(), dao::setId);
                LOGGER.debug("Configured Grouper attribute source");
                list.add(dao);
            }
            return list;
        }

        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            plan.registerAttributeRepositories(grouperAttributeRepositories());
        }
    }


}

