package org.apereo.cas.pm.jdbc;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link JdbcPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class JdbcPasswordManagementService extends BasePasswordManagementService {

    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    private final PasswordEncoder passwordEncoder;

    public JdbcPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         @Nonnull final DataSource dataSource,
                                         @Nonnull final TransactionTemplate transactionTemplate,
                                         final PasswordHistoryService passwordHistoryService,
                                         final PasswordEncoder passwordEncoder) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = transactionTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeRequest bean) {
        var result = this.transactionTemplate.execute(action -> {
            val c = (UsernamePasswordCredential) credential;
            val password = passwordEncoder.encode(bean.getPassword());
            val count = this.jdbcTemplate.update(properties.getJdbc().getSqlChangePassword(), password, c.getId());
            return count > 0;
        });
        return BooleanUtils.toBoolean(result);
    }

    @Override
    public String findEmail(final String username) {
        val query = properties.getJdbc().getSqlFindEmail();
        if (StringUtils.isBlank(query)) {
            LOGGER.debug("No SQL query is defined to retrieve email addresses");
            return null;
        }

        try {
            return this.transactionTemplate.execute(action -> {
                val email = this.jdbcTemplate.queryForObject(query, String.class, username);
                if (StringUtils.isNotBlank(email) && EmailValidator.getInstance().isValid(email)) {
                    return email;
                }
                LOGGER.debug("Username [{}] not found when searching for email", username);
                return null;
            });
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Username [{}] not found when searching for email", username);
            return null;
        }
    }

    @Override
    public String findPhone(final String username) {
        val query = properties.getJdbc().getSqlFindPhone();
        if (StringUtils.isBlank(query)) {
            LOGGER.debug("No SQL query is defined to retrieve phone numbers");
            return null;
        }
        try {
            return this.transactionTemplate.execute(action -> {
                val phone = this.jdbcTemplate.queryForObject(query, String.class, username);
                if (StringUtils.isNotBlank(phone)) {
                    return phone;
                }
                LOGGER.debug("Username [{}] not found when searching for phone", username);
                return null;
            });
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Username [{}] not found when searching for phone", username);
            return null;
        }
    }

    @Override
    public String findUsername(final String email) {
        try {
            return this.transactionTemplate.execute(action -> {
                val username = this.jdbcTemplate.queryForObject(properties.getJdbc().getSqlFindUser(), String.class, email);
                if (StringUtils.isNotBlank(username)) {
                    return username;
                }
                LOGGER.debug("Email [{}] not found when searching for user", email);
                return null;
            });
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Email [{}] not found when searching for user", email);
            return null;
        }
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        return this.transactionTemplate.execute(action -> {
            val sqlSecurityQuestions = properties.getJdbc().getSqlSecurityQuestions();
            val map = new HashMap<String, String>();
            val results = jdbcTemplate.queryForList(sqlSecurityQuestions, username);
            results.forEach(row -> {
                if (row.containsKey("question") && row.containsKey("answer")) {
                    map.put(row.get("question").toString(), row.get("answer").toString());
                }
            });
            LOGGER.debug("Found [{}] security questions for [{}]", map.size(), username);
            return map;
        });
    }
}
