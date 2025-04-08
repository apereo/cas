package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
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

/**
 * This is {@link JdbcDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class JdbcDelegatedIdentityProviderFactory extends BaseDelegatedIdentityProviderFactory {
    private static final String SQL_SELECT = "SELECT * FROM " + JdbcIdentityProviderEntity.TABLE_NAME;
    
    protected final JdbcOperations jdbcTemplate;

    public JdbcDelegatedIdentityProviderFactory(
        final JdbcOperations jdbcTemplate,
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final Cache<String, List<BaseClient>> clientsCache,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, customizers, casSSLContext, clientsCache, applicationContext);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected List<BaseClient> load() throws Exception {
        val configList = fetchIdentityProviderConfiguration();
        val properties = new LinkedHashMap<String, Object>();
        for (val config : configList) {
            val property = config.toCasProperty();
            LOGGER.debug("Loading delegated identity provider configuration [{}]", property);
            properties.put(property, config.getValue());
        }

        if (!properties.isEmpty()) {
            val bound = CasConfigurationProperties.bindFrom(properties);
            if (bound.isPresent()) {
                return buildFrom(bound.get());
            }
        }
        return List.of();
    }

    protected List<JdbcIdentityProviderEntity> fetchIdentityProviderConfiguration() {
        val rowMapper = new BeanPropertyRowMapper<>(JdbcIdentityProviderEntity.class, true);
        return jdbcTemplate.query(SQL_SELECT, rowMapper);
    }

    @Getter
    @Entity
    @Setter
    @Table(name = JdbcIdentityProviderEntity.TABLE_NAME)
    @Accessors(chain = true)
    @ToString
    @EqualsAndHashCode
    public static class JdbcIdentityProviderEntity implements Serializable {
        /**
         * The table name that holds the config in the database.
         */
        public static final String TABLE_NAME = "JdbcIdentityProviderEntity";
        private static final String CONFIG_PREFIX = "cas.authn.pac4j";
        @Serial
        private static final long serialVersionUID = 976705073390152323L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        private String type;
        private int index;
        private String name;
        private String value;

        String toCasProperty() {
            return CONFIG_PREFIX + ".%s[%s].%s".formatted(getType(), getIndex(), getName());
        }
    }
}
