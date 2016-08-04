package org.apereo.cas.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.jdbc.AbstractJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.MultiRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean(name = {"stubAttributeRepository", "attributeRepository"})
    public IPersonAttributeDao stubAttributeRepository() {
        final MergingPersonAttributeDaoImpl mergingDao = new MergingPersonAttributeDaoImpl();
        mergingDao.setMerger(new ReplacingAttributeAdder());

        final List<IPersonAttributeDao> list = new ArrayList<>();
        
        // Add LDAP
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
            list.add(ldapDao);
        }

        // Add various forms of JDBC
        final PrincipalAttributesProperties.Jdbc jdbc = casProperties.getAuthn().getAttributeRepository().getJdbc();
        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty() && StringUtils.isNotBlank(jdbc.getSql())) {
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
            jdbcDao.setResultAttributeMapping(casProperties.getAuthn().getAttributeRepository().getAttributes());
            jdbcDao.setRequireAllQueryAttributes(jdbc.isRequireAllAttributes());
            jdbcDao.setUsernameCaseCanonicalizationMode(jdbc.getCaseCanonicalization());
            jdbcDao.setQueryType(jdbc.getQueryType());
            
            list.add(mergingDao);
        }

        // Add stub
        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty()) {
            list.add(Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository()));
        }
        
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
}

