package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.ProcedureJdbcAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link StoredProcedureAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class StoredProcedureAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler<ProcedureJdbcAuthenticationProperties> {

    public StoredProcedureAuthenticationHandler(
        final ProcedureJdbcAuthenticationProperties properties,
        final PrincipalFactory principalFactory, final DataSource dataSource) {
        super(properties, principalFactory, dataSource);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {
        val username = credential.getUsername();
        val password = credential.toPassword();

        val jdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName(properties.getProcedureName());
        val results = jdbcCall.execute(Map.of("username", username, "password", password));
        LOGGER.debug("Procedure results are [{}]", results);
        if (results.isEmpty() || !BooleanUtils.toBoolean(results.get("status").toString())) {
            throw new FailedLoginException("Failed to authenticate user");
        }
        val principal = principalFactory.createPrincipal(username, CollectionUtils.toMultiValuedMap(results));
        return createHandlerResult(credential, principal, new ArrayList<>());
    }
}
