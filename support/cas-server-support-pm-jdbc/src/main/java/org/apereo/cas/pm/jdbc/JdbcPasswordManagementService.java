package org.apereo.cas.pm.jdbc;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionOperations;
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
@Getter
public class JdbcPasswordManagementService extends BasePasswordManagementService {

    private final JdbcTemplate jdbcTemplate;

    private final TransactionOperations transactionTemplate;

    private final PasswordEncoder passwordEncoder;

    public JdbcPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final CasConfigurationProperties casProperties,
                                         @NonNull final DataSource dataSource,
                                         @NonNull final TransactionOperations transactionTemplate,
                                         final PasswordHistoryService passwordHistoryService,
                                         final PasswordEncoder passwordEncoder) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = transactionTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) {
        var result = this.transactionTemplate.execute(action -> {
            val password = passwordEncoder.encode(bean.toPassword());
            val count = this.jdbcTemplate.update(casProperties.getAuthn().getPm().getJdbc().getSqlChangePassword(), password, bean.getUsername());
            return count > 0;
        });
        return BooleanUtils.toBoolean(result);
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val queryFindEmail = casProperties.getAuthn().getPm().getJdbc().getSqlFindEmail();
        if (StringUtils.isBlank(queryFindEmail)) {
            LOGGER.debug("No SQL query is defined to retrieve email addresses");
            return null;
        }

        try {
            return this.transactionTemplate.execute(action -> {
                val email = this.jdbcTemplate.queryForObject(queryFindEmail, String.class, query.getUsername());
                if (StringUtils.isNotBlank(email) && EmailValidator.getInstance().isValid(email)) {
                    return email;
                }
                LOGGER.debug("Username [{}] not found when searching for email", query.getUsername());
                return null;
            });
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Username [{}] not found when searching for email", query.getUsername());
            return null;
        }
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        val findPhone = casProperties.getAuthn().getPm().getJdbc().getSqlFindPhone();
        if (StringUtils.isBlank(findPhone)) {
            LOGGER.debug("No SQL query is defined to retrieve phone numbers");
            return null;
        }
        try {
            return this.transactionTemplate.execute(action -> {
                val phone = this.jdbcTemplate.queryForObject(findPhone, String.class, query.getUsername());
                if (StringUtils.isNotBlank(phone)) {
                    return phone;
                }
                LOGGER.debug("Username [{}] not found when searching for phone", query.getUsername());
                return null;
            });
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Username [{}] not found when searching for phone", query.getUsername());
            return null;
        }
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        try {
            return transactionTemplate.execute(action ->
                jdbcTemplate.queryForObject(casProperties.getAuthn().getPm().getJdbc().getSqlFindUser(), String.class, query.getEmail()));
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Email [{}] not found when searching for user", query.getEmail());
            return null;
        }
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        return this.transactionTemplate.execute(action -> {
            val sqlSecurityQuestions = casProperties.getAuthn().getPm().getJdbc().getSqlGetSecurityQuestions();
            val map = new HashMap<String, String>();
            val results = jdbcTemplate.queryForList(sqlSecurityQuestions, query.getUsername());
            results.forEach(row -> {
                if (row.containsKey("question") && row.containsKey("answer")) {
                    map.put(row.get("question").toString(), row.get("answer").toString());
                }
            });
            LOGGER.debug("Found [{}] security questions for [{}]", map.size(), query.getUsername());
            return map;
        });
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        jdbcTemplate.update(casProperties.getAuthn().getPm().getJdbc().getSqlDeleteSecurityQuestions(), query.getUsername());
        query.getSecurityQuestions().forEach((question, values) -> values.forEach(answer ->
            jdbcTemplate.update(casProperties.getAuthn().getPm().getJdbc().getSqlUpdateSecurityQuestions(),
                query.getUsername(), question, answer)));
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        return jdbcTemplate.update(casProperties.getAuthn().getPm().getJdbc().getSqlUnlockAccount(), Boolean.TRUE, credential.getId()) > 0;
    }
}
