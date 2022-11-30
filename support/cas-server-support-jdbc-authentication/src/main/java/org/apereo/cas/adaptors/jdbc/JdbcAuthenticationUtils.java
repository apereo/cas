package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A JDBC utility class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@UtilityClass
@Slf4j
public class JdbcAuthenticationUtils {

    /**
     * Configure a JDBC authentication handler.
     *
     * @param handler the authn handler
     * @param config the password policy
     * @param properties the JDBC properties
     * @param applicationContext the application context
     */
    public static void configureJdbcAuthenticationHandler(final AbstractJdbcUsernamePasswordAuthenticationHandler handler,
                                                          final PasswordPolicyContext config,
                                                          final BaseJdbcAuthenticationProperties properties,
                                                          final ConfigurableApplicationContext applicationContext) {
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(properties.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties.getPrincipalTransformation()));
        handler.setPasswordPolicyConfiguration(config);
        handler.setState(properties.getState());
        if (StringUtils.isNotBlank(properties.getCredentialCriteria())) {
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(properties.getCredentialCriteria()));
        }
        LOGGER.trace("Configured authentication handler [{}] to handle database url at [{}]", handler.getName(), properties.getName());
    }
}
