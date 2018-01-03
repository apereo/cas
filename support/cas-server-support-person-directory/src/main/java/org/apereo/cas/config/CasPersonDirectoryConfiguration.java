package org.apereo.cas.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.resolvers.InternalGroovyScriptDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.GrouperPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
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
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;

import javax.naming.directory.SearchControls;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasPersonDirectoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casPersonDirectoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasPersonDirectoryConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "attributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> attributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();

        list.addAll(ldapAttributeRepositories());
        list.addAll(jdbcAttributeRepositories());
        list.addAll(jsonAttributeRepositories());
        list.addAll(groovyAttributeRepositories());
        list.addAll(grouperAttributeRepositories());
        list.addAll(restfulAttributeRepositories());
        list.addAll(scriptedAttributeRepositories());
        list.addAll(stubAttributeRepositories());

        OrderComparator.sort(list);

        LOGGER.debug("Final list of attribute repositories is [{}]", list);
        return list;
    }

    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean
    @RefreshScope
    public IPersonAttributeDao attributeRepository() {
        return cachingAttributeRepository();
    }

    @ConditionalOnMissingBean(name = "jsonAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> jsonAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        casProperties.getAuthn().getAttributeRepository().getJson().forEach(Unchecked.consumer(json -> {
            final Resource r = json.getLocation();
            if (r != null) {
                final JsonBackedComplexStubPersonAttributeDao dao = new JsonBackedComplexStubPersonAttributeDao(r);
                dao.setOrder(json.getOrder());
                dao.init();
                LOGGER.debug("Configured JSON attribute sources from [{}]", r);
                list.add(dao);
            }
        }));
        return list;
    }

    @ConditionalOnMissingBean(name = "groovyAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> groovyAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        casProperties.getAuthn().getAttributeRepository().getGroovy().forEach(groovy -> {
            if (groovy.getLocation() != null) {
                final GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties));
                dao.setCaseInsensitiveUsername(groovy.isCaseInsensitive());
                dao.setOrder(groovy.getOrder());

                LOGGER.debug("Configured Groovy attribute sources from [{}]", groovy.getLocation());
                list.add(dao);
            }
        });
        return list;
    }

    @ConditionalOnMissingBean(name = "grouperAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> grouperAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        final GrouperPrincipalAttributesProperties gp = casProperties.getAuthn().getAttributeRepository().getGrouper();

        if (gp.isEnabled()) {
            final GrouperPersonAttributeDao dao = new GrouperPersonAttributeDao();
            dao.setOrder(gp.getOrder());
            LOGGER.debug("Configured Grouper attribute source");
            list.add(dao);
        }
        return list;
    }

    @ConditionalOnMissingBean(name = "stubAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> stubAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        final Map<String, String> attrs = casProperties.getAuthn().getAttributeRepository().getStub().getAttributes();
        if (!attrs.isEmpty() && list.isEmpty()) {
            LOGGER.info("Found and added static attributes [{}] to the list of candidate attribute repositories", attrs.keySet());
            list.add(Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository()));
        }
        return list;
    }

    @ConditionalOnMissingBean(name = "jdbcAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> jdbcAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        final PrincipalAttributesProperties attrs = casProperties.getAuthn().getAttributeRepository();
        attrs.getJdbc().forEach(jdbc -> {
            if (StringUtils.isNotBlank(jdbc.getSql()) && StringUtils.isNotBlank(jdbc.getUrl())) {
                final AbstractJdbcPersonAttributeDao jdbcDao;

                if (jdbc.isSingleRow()) {
                    LOGGER.debug("Configured single-row JDBC attribute repository for [{}]", jdbc.getUrl());
                    jdbcDao = new SingleRowJdbcPersonAttributeDao(
                            JpaBeans.newDataSource(jdbc),
                            jdbc.getSql()
                    );
                } else {
                    LOGGER.debug("Configured multi-row JDBC attribute repository for [{}]", jdbc.getUrl());
                    jdbcDao = new MultiRowJdbcPersonAttributeDao(
                            JpaBeans.newDataSource(jdbc),
                            jdbc.getSql()
                    );
                    LOGGER.debug("Configured multi-row JDBC column mappings for [{}] are [{}]", jdbc.getUrl(), jdbc.getColumnMappings());
                    ((MultiRowJdbcPersonAttributeDao) jdbcDao).setNameValueColumnMappings(jdbc.getColumnMappings());
                }

                jdbcDao.setQueryAttributeMapping(CollectionUtils.wrap("username", jdbc.getUsername()));
                final Map<String, String> mapping = jdbc.getAttributes();
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
            }
        });
        return list;
    }

    @ConditionalOnMissingBean(name = "ldapAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> ldapAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        final PrincipalAttributesProperties attrs = casProperties.getAuthn().getAttributeRepository();
        attrs.getLdap().forEach(ldap -> {
            if (StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getLdapUrl())) {
                final LdaptivePersonAttributeDao ldapDao = new LdaptivePersonAttributeDao();

                LOGGER.debug("Configured LDAP attribute source for [{}] and baseDn [{}]", ldap.getLdapUrl(), ldap.getBaseDn());
                ldapDao.setConnectionFactory(LdapUtils.newLdaptivePooledConnectionFactory(ldap));
                ldapDao.setBaseDN(ldap.getBaseDn());

                LOGGER.debug("LDAP attributes are fetched from [{}] via filter [{}]", ldap.getLdapUrl(), ldap.getUserFilter());
                ldapDao.setSearchFilter(ldap.getUserFilter());

                final SearchControls constraints = new SearchControls();
                if (ldap.getAttributes() != null && !ldap.getAttributes().isEmpty()) {
                    LOGGER.debug("Configured result attribute mapping for [{}] to be [{}]", ldap.getLdapUrl(), ldap.getAttributes());
                    ldapDao.setResultAttributeMapping(ldap.getAttributes());
                    final String[] attributes = ldap.getAttributes().keySet().toArray(new String[ldap.getAttributes().keySet().size()]);
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

                LOGGER.debug("Initializing LDAP attribute source for [{}]", ldap.getLdapUrl());
                ldapDao.initialize();

                list.add(ldapDao);
            }
        });

        return list;
    }

    @ConditionalOnMissingBean(name = "scriptedAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> scriptedAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        casProperties.getAuthn().getAttributeRepository().getScript()
                .forEach(Unchecked.consumer(script -> {
                    final ScriptEnginePersonAttributeDao dao = new ScriptEnginePersonAttributeDao();
                    final String scriptFile = IOUtils.toString(script.getLocation().getInputStream(), StandardCharsets.UTF_8);
                    dao.setScriptFile(scriptFile);
                    dao.setCaseInsensitiveUsername(script.isCaseInsensitive());
                    dao.setOrder(script.getOrder());
                    LOGGER.debug("Configured scripted attribute sources from [{}]", script.getLocation());
                    list.add(dao);
                }));
        return list;
    }

    @ConditionalOnMissingBean(name = "restfulAttributeRepositories")
    @Bean
    @RefreshScope
    public List<IPersonAttributeDao> restfulAttributeRepositories() {
        final List<IPersonAttributeDao> list = new ArrayList<>();
        casProperties.getAuthn().getAttributeRepository().getRest().forEach(rest -> {
            if (StringUtils.isNotBlank(rest.getUrl())) {

                final RestfulPersonAttributeDao dao = new RestfulPersonAttributeDao();
                dao.setCaseInsensitiveUsername(rest.isCaseInsensitive());
                dao.setOrder(rest.getOrder());
                dao.setUrl(rest.getUrl());
                dao.setMethod(HttpMethod.resolve(rest.getMethod()).name());

                if (StringUtils.isNotBlank(rest.getBasicAuthPassword()) && StringUtils.isNotBlank(rest.getBasicAuthUsername())) {
                    dao.setBasicAuthPassword(rest.getBasicAuthPassword());
                    dao.setBasicAuthUsername(rest.getBasicAuthUsername());
                    LOGGER.debug("Basic authentication credentials are located for REST endpoint [{}]", rest.getUrl());
                } else {
                    LOGGER.debug("Basic authentication credentials are not defined for REST endpoint [{}]", rest.getUrl());
                }

                LOGGER.debug("Configured REST attribute sources from [{}]", rest.getUrl());
                list.add(dao);
            }
        });

        return list;
    }

    @Bean
    @ConditionalOnMissingBean(name = "cachingAttributeRepository")
    public IPersonAttributeDao cachingAttributeRepository() {
        final CachingPersonAttributeDaoImpl impl = new CachingPersonAttributeDaoImpl();
        impl.setCacheNullResults(false);

        final Cache graphs = Caffeine.newBuilder()
                .maximumSize(casProperties.getAuthn().getAttributeRepository().getMaximumCacheSize())
                .expireAfterWrite(casProperties.getAuthn().getAttributeRepository().getExpireInMinutes(), TimeUnit.MINUTES)
                .build();
        impl.setUserInfoCache(graphs.asMap());
        impl.setCachedPersonAttributesDao(aggregatingAttributeRepository());

        LOGGER.debug("Configured cache expiration policy for merging attribute sources to be [{}] minute(s)",
                casProperties.getAuthn().getAttributeRepository().getExpireInMinutes());
        return impl;
    }

    @Bean
    @ConditionalOnMissingBean(name = "aggregatingAttributeRepository")
    public IPersonAttributeDao aggregatingAttributeRepository() {
        final MergingPersonAttributeDaoImpl mergingDao = new MergingPersonAttributeDaoImpl();
        final String merger = StringUtils.defaultIfBlank(casProperties.getAuthn().getAttributeRepository().getMerger(), "replace".trim());
        LOGGER.debug("Configured merging strategy for attribute sources is [{}]", merger);
        mergingDao.setMerger(getAttributeMerger(merger));

        final List<IPersonAttributeDao> list = attributeRepositories();
        mergingDao.setPersonAttributeDaos(list);

        if (list.isEmpty()) {
            LOGGER.debug("No attribute repository sources are available/defined to merge together.");
        } else {
            LOGGER.debug("Configured attribute repository sources to merge together: [{}]", list);
        }

        return mergingDao;
    }

    private IAttributeMerger getAttributeMerger(final String merger) {
        switch (merger.toLowerCase()) {
            case "merge":
                return new MultivaluedAttributeMerger();
            case "add":
                return new NoncollidingAttributeAdder();
            case "replace":
            default:
                return new ReplacingAttributeAdder();
        }
    }

}

