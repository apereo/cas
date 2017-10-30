package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.NoResultException;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link SurrogateJdbcAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateJdbcAuthenticationService extends BaseSurrogateAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateJdbcAuthenticationService.class);

    private final JdbcTemplate jdbcTemplate;
    private final String surrogateSearchQuery;
    private final String surrogateAccountQuery;

    public SurrogateJdbcAuthenticationService(final String surrogateSearchQuery, final DataSource dataSource,
                                              final String surrogateAccountQuery,
                                              final ServicesManager servicesManager) {
        super(servicesManager);
        this.surrogateSearchQuery = surrogateSearchQuery;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.surrogateAccountQuery = surrogateAccountQuery;
    }

    @Override
    public boolean canAuthenticateAsInternal(final String username, final Principal surrogate, final Service service) {
        try {
            if (username.equalsIgnoreCase(surrogate.getId())) {
                return true;
            }
            LOGGER.debug("Executing SQL query [{}]", surrogateSearchQuery);
            final int count = this.jdbcTemplate.queryForObject(surrogateSearchQuery, Integer.class, username);
            return count > 0;
        } catch (final NoResultException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        try {
            final List<SurrogateAccount> results = this.jdbcTemplate.query(this.surrogateAccountQuery,
                    new BeanPropertyRowMapper<>(SurrogateAccount.class));
            return results
                    .stream()
                    .map(SurrogateAccount::getSurrogateAccount)
                    .collect(Collectors.toSet());

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    /**
     * The type Surrogate account.
     */
    public static class SurrogateAccount implements Serializable {
        private static final long serialVersionUID = 7734857552147825153L;

        private String surrogateAccount;

        public String getSurrogateAccount() {
            return surrogateAccount;
        }

        public void setSurrogateAccount(final String surrogateAccount) {
            this.surrogateAccount = surrogateAccount;
        }
    }
}
