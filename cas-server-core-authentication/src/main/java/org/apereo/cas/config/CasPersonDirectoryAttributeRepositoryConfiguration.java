package org.apereo.cas.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.BaseGroovyScriptDaoImpl;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.GroovyPersonAttributeDao;
import org.apereo.services.persondir.support.JsonBackedComplexStubPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.MultiRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.naming.directory.SearchControls;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasPersonDirectoryAttributeRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casPersonDirectoryAttributeRepositoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasPersonDirectoryAttributeRepositoryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasPersonDirectoryAttributeRepositoryConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean(name = {"stubAttributeRepository", "attributeRepository"})
    public IPersonAttributeDao attributeRepository() {
        final List<IPersonAttributeDao> list = new ArrayList<>();

        addLdapAttributeRepository(list);
        addJdbcAttributeRepository(list);
        addJsonAttributeRepository(list);
        addGroovyAttributeRepository(list);
        addStubAttributeRepositoryIfNothingElse(list);

        return composeMergedAndCachedAttributeRepositories(list);
    }

    private void addJsonAttributeRepository(final List<IPersonAttributeDao> list) {
        final Resource r = casProperties.getAuthn().getAttributeRepository().getJson().getConfig().getLocation();
        if (r != null) {
            final JsonBackedComplexStubPersonAttributeDao dao = new JsonBackedComplexStubPersonAttributeDao(r);
            list.add(dao);
        }
    }

    private void addGroovyAttributeRepository(final List<IPersonAttributeDao> list) {
        final PrincipalAttributesProperties.Groovy groovy = casProperties.getAuthn().getAttributeRepository().getGroovy();
        if (groovy.getConfig().getLocation() != null) {
            final GroovyPersonAttributeDao dao = new GroovyPersonAttributeDao(new GroovyScriptDao(applicationContext, casProperties));
            dao.setCaseInsensitiveUsername(groovy.isCaseInsensitive());
            list.add(dao);
        }
    }

    private IPersonAttributeDao composeMergedAndCachedAttributeRepositories(final List<IPersonAttributeDao> list) {
        final MergingPersonAttributeDaoImpl mergingDao = new MergingPersonAttributeDaoImpl();
        mergingDao.setMerger(new ReplacingAttributeAdder());
        mergingDao.setPersonAttributeDaos(list);

        final CachingPersonAttributeDaoImpl impl = new CachingPersonAttributeDaoImpl();
        impl.setCacheNullResults(false);

        final Cache graphs = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .weakKeys()
                .maximumSize(casProperties.getAuthn().getAttributeRepository().getMaximumCacheSize())
                .expireAfterWrite(casProperties.getAuthn().getAttributeRepository().getExpireInMinutes(), TimeUnit.MINUTES).build();
        impl.setUserInfoCache(graphs.asMap());
        impl.setCachedPersonAttributesDao(mergingDao);

        return impl;
    }

    private void addStubAttributeRepositoryIfNothingElse(final List<IPersonAttributeDao> list) {
        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty() && list.isEmpty()) {
            list.add(Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository()));
        }
    }

    private void addJdbcAttributeRepository(final List<IPersonAttributeDao> list) {
        final PrincipalAttributesProperties.Jdbc jdbc = casProperties.getAuthn().getAttributeRepository().getJdbc();
        if (StringUtils.isNotBlank(jdbc.getSql())) {
            final AbstractJdbcPersonAttributeDao jdbcDao;

            if (jdbc.isSingleRow()) {
                jdbcDao = new SingleRowJdbcPersonAttributeDao(
                        Beans.newHickariDataSource(jdbc),
                        jdbc.getSql()
                );
            } else {
                jdbcDao = new MultiRowJdbcPersonAttributeDao(
                        Beans.newHickariDataSource(jdbc),
                        jdbc.getSql()
                );
                ((MultiRowJdbcPersonAttributeDao) jdbcDao).setNameValueColumnMappings(jdbc.getColumnMappings());
            }

            jdbcDao.setQueryAttributeMapping(ImmutableMap.of("username", jdbc.getUsername()));
            Map<String, String> mapping = casProperties.getAuthn().getAttributeRepository().getAttributes();
            if (mapping != null && mapping.size() > 0)
                jdbcDao.setResultAttributeMapping(mapping);
            jdbcDao.setRequireAllQueryAttributes(jdbc.isRequireAllAttributes());
            jdbcDao.setUsernameCaseCanonicalizationMode(jdbc.getCaseCanonicalization());
            jdbcDao.setQueryType(jdbc.getQueryType());
            list.add(jdbcDao);
        }
    }

    private void addLdapAttributeRepository(final List<IPersonAttributeDao> list) {
        final PrincipalAttributesProperties.Ldap ldap = casProperties.getAuthn().getAttributeRepository().getLdap();
        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty()
                && StringUtils.isNotBlank(ldap.getBaseDn())
                && StringUtils.isNotBlank(ldap.getLdapUrl())) {

            final LdaptivePersonAttributeDao ldapDao = new LdaptivePersonAttributeDao();
            ldapDao.setConnectionFactory(Beans.newPooledConnectionFactory(ldap));
            ldapDao.setBaseDN(ldap.getBaseDn());
            ldapDao.setSearchFilter(ldap.getUserFilter());
            ldapDao.setResultAttributeMapping(casProperties.getAuthn().getAttributeRepository().getAttributes());

            final SearchControls constraints = new SearchControls();

            final String[] attributes = casProperties.getAuthn().getAttributeRepository().getAttributes().keySet()
                    .toArray(new String[casProperties.getAuthn().getAttributeRepository().getAttributes().keySet().size()]);
            constraints.setReturningAttributes(attributes);

            if (ldap.isSubtreeSearch()) {
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            }
            constraints.setDerefLinkFlag(true);
            ldapDao.setSearchControls(constraints);
            ldapDao.initialize();
            list.add(ldapDao);
        }
    }
    
    private static class GroovyScriptDao extends BaseGroovyScriptDaoImpl {
        private ApplicationContext applicationContext;
        private CasConfigurationProperties casProperties;

        GroovyScriptDao(final ApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
            this.applicationContext = applicationContext;
            this.casProperties = casProperties;
        }

        @Override
        public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(final Map<String, List<Object>> attributes) {
            if (attributes.containsKey("username")) {
                final List<Object> a = attributes.get("username");
                if (!a.isEmpty()) {
                    final Map<String, List<Object>> results = new HashMap<>();
                    final Map<String, Object> attrs = getAttributesForUser(a.get(0).toString());
                    attrs.forEach((k, v) -> {
                        final List<Object> values = new ArrayList<>();
                        values.addAll(CollectionUtils.convertValueToCollection(v));
                        results.put(k, values);
                    });
                    return results;
                }
            }
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getAttributesForUser(final String uid) {
            final PrincipalAttributesProperties.Groovy groovy = casProperties.getAuthn().getAttributeRepository().getGroovy();
            
            final ClassLoader parent = getClass().getClassLoader();
            try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
                if (groovy.getConfig().getLocation() != null) {
                    final File groovyFile = groovy.getConfig().getLocation().getFile();
                    if (groovyFile.exists()) {
                        final Class<?> groovyClass = loader.parseClass(groovyFile);
                        LOGGER.debug("Loaded groovy class {} from script {}", groovyClass.getSimpleName(), groovyFile.getCanonicalPath());
                        final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                        LOGGER.debug("Created groovy object instance from class {}", groovyFile.getCanonicalPath());
                        final Object[] args = {uid, LOGGER, casProperties, applicationContext};
                        LOGGER.debug("Executing groovy script's run method, with parameters {}", args);
                        final Map<String, Object> personAttributesMap = (Map<String, Object>) groovyObject.invokeMethod("run", args);
                        LOGGER.debug("Creating person attributes with the username {} and attributes {}", uid, personAttributesMap);
                        return personAttributesMap;
                    }
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return new HashMap<>();
        }
    }
}

