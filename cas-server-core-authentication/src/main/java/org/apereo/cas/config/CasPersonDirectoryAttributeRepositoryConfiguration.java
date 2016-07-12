package org.apereo.cas.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.apereo.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
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
        final CachingPersonAttributeDaoImpl impl = new CachingPersonAttributeDaoImpl();
        impl.setCacheNullResults(false);

        final Cache graphs = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .weakKeys()
                .maximumSize(casProperties.getAuthn().getAttributeRepository().getMaximumCacheSize())
                .expireAfterWrite(casProperties.getAuthn().getAttributeRepository().getExpireInMinutes(), TimeUnit.MINUTES).build();
        impl.setUserInfoCache(graphs.asMap());

        final MergingPersonAttributeDaoImpl dao = new MergingPersonAttributeDaoImpl();
        dao.setMerger(new NoncollidingAttributeAdder());
        dao.setMerger(new ReplacingAttributeAdder());
        dao.setMerger(new MultivaluedAttributeMerger());

        final List list = new ArrayList<>();

        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty()) {
            list.add(Beans.newAttributeRepository(casProperties.getAuthn().getAttributeRepository().getAttributes()));
        }

        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty()) {

            final LdaptivePersonAttributeDao ldap = new LdaptivePersonAttributeDao();
            ldap.setConnectionFactory(Beans.newPooledConnectionFactory(casProperties.getAuthn().getAttributeRepository().getLdap()));
            ldap.setBaseDN(casProperties.getAuthn().getAttributeRepository().getLdap().getBaseDn());
            ldap.setSearchFilter(casProperties.getAuthn().getAttributeRepository().getLdap().getUserFilter());
            ldap.setResultAttributeMapping(casProperties.getAuthn().getAttributeRepository().getAttributes());

            final SearchControls constraints = new SearchControls();
            constraints.setReturningAttributes((String[]) casProperties.getAuthn()
                    .getAttributeRepository().getAttributes().keySet().toArray());

            if (casProperties.getAuthn().getAttributeRepository().getLdap().isSubtreeSearch()) {
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            }
            constraints.setDerefLinkFlag(true);
            ldap.setSearchControls(constraints);
            list.add(ldap);
        }

        if (!casProperties.getAuthn().getAttributeRepository().getAttributes().isEmpty()
            && StringUtils.isNotBlank(casProperties.getAuthn().getAttributeRepository().getJdbc().getSql())) {

            final SingleRowJdbcPersonAttributeDao jdbc = new SingleRowJdbcPersonAttributeDao(
                    Beans.newHickariDataSource(casProperties.getAuthn().getAttributeRepository().getJdbc()),
                    casProperties.getAuthn().getAttributeRepository().getJdbc().getSql()
            );
            jdbc.setQueryAttributeMapping(ImmutableMap.of("username",
                    casProperties.getAuthn().getAttributeRepository().getJdbc().getUsername()));
            jdbc.setResultAttributeMapping(casProperties.getAuthn().getAttributeRepository().getAttributes());
            list.add(jdbc);
        }

        dao.setPersonAttributeDaos(list);

        return dao;
    }
}

