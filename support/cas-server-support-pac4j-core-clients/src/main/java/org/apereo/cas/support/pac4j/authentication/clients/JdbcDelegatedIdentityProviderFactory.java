package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * This is {@link JdbcDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class JdbcDelegatedIdentityProviderFactory extends BaseDelegatedIdentityProviderFactory {
    protected final JdbcOperations jdbcTemplate;

    public JdbcDelegatedIdentityProviderFactory(
        final JdbcOperations jdbcTemplate,
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final Cache<String, Collection<BaseClient>> clientsCache,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, customizers, casSSLContext, clientsCache, applicationContext);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected Collection<BaseClient> loadIdentityProviders() throws Exception {
        val sql = "SELECT * FROM " + JdbcIdentityProviderEntity.TABLE_NAME;
        val rowMapper = new BeanPropertyRowMapper<>(JdbcIdentityProviderEntity.class, true);
        val configList = jdbcTemplate.query(sql, rowMapper);

        val prefix = "cas.authn.pac4j";
        val properties = new LinkedHashMap<String, Object>();
        for (val config : configList) {
            switch (config.getType().toLowerCase(Locale.ENGLISH)) {
                case "cas":
                    properties.put((prefix + ".cas[%s].%s").formatted(config.getIndex(), config.getName()), config.getValue());
                    break;
                case "oauth2":
                case "oauth":
                    properties.put((prefix + ".oauth2[%s].%s").formatted(config.getIndex(), config.getName()), config.getValue());
                    break;
                case "oidc":
                    properties.put((prefix + ".oidc[%s].%s").formatted(config.getIndex(), config.getName()), config.getValue());
                    break;
                case "saml":
                case "saml2":
                    properties.put((prefix + ".saml[%s].%s").formatted(config.getIndex(), config.getName()), config.getValue());
                    break;
            }
        }
        
        if (!properties.isEmpty()) {
            val bound = CasConfigurationProperties.bindFrom(getClass().getSimpleName(), properties);
            if (bound.isPresent()) {
                return buildAllIdentityProviders(bound.get());
            }
        }
        return List.of();
    }

    @Getter
    @Entity
    @Setter
    @Table(name = JdbcIdentityProviderEntity.TABLE_NAME)
    @Accessors(chain = true)
    public static class JdbcIdentityProviderEntity implements Serializable {
        @Serial
        private static final long serialVersionUID = 976705073390152323L;

        public static final String TABLE_NAME = "JdbcIdentityProviderEntity";
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        private String type;
        private int index;
        private String name;
        private String value;
    }
}
