package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.NoResultException;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final String surrogateSearchQuery;

    private final String surrogateAccountQuery;

    public SurrogateJdbcAuthenticationService(final String surrogateSearchQuery, final DataSource dataSource,
                                              final String surrogateAccountQuery, final ServicesManager servicesManager) {
        super(servicesManager);
        this.surrogateSearchQuery = surrogateSearchQuery;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.surrogateAccountQuery = surrogateAccountQuery;
    }

    @Override
    public boolean canAuthenticateAsInternal(final String username, final Principal surrogate, final Optional<Service> service) {
        try {
            if (username.equalsIgnoreCase(surrogate.getId())) {
                return true;
            }
            LOGGER.debug("Executing SQL query [{}]", surrogateSearchQuery);
            val count = this.jdbcTemplate.queryForObject(surrogateSearchQuery, Integer.class, surrogate.getId(), username);
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
            val results = this.jdbcTemplate.query(this.surrogateAccountQuery,
                new BeanPropertyRowMapper<>(SurrogateAccount.class), username);
            return results.stream().map(SurrogateAccount::getSurrogateAccount).collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
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
        private static final long serialVersionUID = 7734857552147825153L;
        private String surrogateAccount;
    }
}
