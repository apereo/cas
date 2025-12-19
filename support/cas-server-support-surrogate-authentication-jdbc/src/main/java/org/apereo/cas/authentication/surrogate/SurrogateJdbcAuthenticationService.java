package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link SurrogateJdbcAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class SurrogateJdbcAuthenticationService extends BaseSurrogateAuthenticationService {

    private final JdbcTemplate jdbcTemplate;

    public SurrogateJdbcAuthenticationService(final JdbcTemplate jdbcTemplate,
                                              final ServicesManager servicesManager,
                                              final CasConfigurationProperties casProperties,
                                              final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                              final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean canImpersonateInternal(final String username, final Principal surrogate, final Optional<? extends Service> service) {
        val surrogateSearchQuery = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(casProperties.getAuthn().getSurrogate().getJdbc().getSurrogateSearchQuery());
        LOGGER.debug("Executing SQL query [{}]", surrogateSearchQuery);
        val count = jdbcTemplate.queryForObject(surrogateSearchQuery, Integer.class, surrogate.getId(), username);
        return Objects.requireNonNull(count) > 0;
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) {
        val surrogateAccountQuery = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(casProperties.getAuthn().getSurrogate().getJdbc().getSurrogateAccountQuery());
        val results = jdbcTemplate.query(surrogateAccountQuery, new BeanPropertyRowMapper<>(SurrogateAccount.class), username);
        return results.parallelStream().map(SurrogateAccount::getSurrogateAccount).collect(Collectors.toList());
    }

    /**
     * The type Surrogate account.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class SurrogateAccount implements Serializable {
        @Serial
        private static final long serialVersionUID = 7734857552147825153L;

        private String surrogateAccount;
    }
}
