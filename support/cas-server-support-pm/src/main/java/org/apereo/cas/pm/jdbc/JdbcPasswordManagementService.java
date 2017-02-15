package org.apereo.cas.pm.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link JdbcPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JdbcPasswordManagementService extends BasePasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPasswordManagementService.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor, 
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         final DataSource dataSource) {
        super(cipherExecutor, issuer, passwordManagementProperties);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Audit(action = "CHANGE_PASSWORD",
            actionResolverName = "CHANGE_PASSWORD_ACTION_RESOLVER",
            resourceResolverName = "CHANGE_PASSWORD_RESOURCE_RESOLVER")
    @Override
    public boolean change(final Credential credential, final PasswordChangeBean bean) {
        Assert.notNull(credential, "Credential cannot be null");
        Assert.notNull(bean, "PasswordChangeBean cannot be null");
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        final PasswordEncoder encoder = Beans.newPasswordEncoder(passwordManagementProperties.getJdbc().getPasswordEncoder());
        final String password = encoder.encode(bean.getPassword());
        final int count = this.jdbcTemplate.update(passwordManagementProperties.getJdbc().getSqlChangePassword(), password, c.getId());
        return count > 0;
    }

    @Override
    public String findEmail(final String username) {
        final String email = this.jdbcTemplate.queryForObject(passwordManagementProperties.getJdbc().getSqlFindEmail(),
                String.class, username);
        if (StringUtils.isNotBlank(email) && EmailValidator.getInstance().isValid(email)) {
            return email;
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final Map map = jdbcTemplate.queryForMap(passwordManagementProperties.getJdbc().getSqlSecurityQuestions(), username);
        LOGGER.debug("Found [{}] security questions for [{}]", map.size(), username);
        return map;
    }
}
