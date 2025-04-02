package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BindJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.ProcedureJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import javax.sql.DataSource;

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
     * @param handler            the authn handler
     * @param config             the password policy
     * @param properties         the JDBC properties
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

    /**
     * New authentication handler.
     *
     * @param properties           the properties
     * @param applicationContext   the application context
     * @param jdbcPrincipalFactory the jdbc principal factory
     * @param servicesManager      the services manager
     * @param passwordPolicy       the password policy
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final BindJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext passwordPolicy) {
        val handler = new BindModeSearchDatabaseAuthenticationHandler(properties, servicesManager,
            jdbcPrincipalFactory, JpaBeans.newDataSource(properties));
        configureJdbcAuthenticationHandler(handler, passwordPolicy, properties, applicationContext);
        return handler;
    }

    /**
     * New authentication handler.
     *
     * @param properties                                the properties
     * @param applicationContext                        the application context
     * @param jdbcPrincipalFactory                      the jdbc principal factory
     * @param servicesManager                           the services manager
     * @param queryAndEncodePasswordPolicyConfiguration the query and encode password policy configuration
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration) {
        return newAuthenticationHandler(properties, applicationContext, jdbcPrincipalFactory,
            servicesManager, queryAndEncodePasswordPolicyConfiguration, JpaBeans.newDataSource(properties));
    }

    /**
     * New authentication handler.
     *
     * @param properties                                the properties
     * @param applicationContext                        the application context
     * @param jdbcPrincipalFactory                      the jdbc principal factory
     * @param servicesManager                           the services manager
     * @param queryAndEncodePasswordPolicyConfiguration the query and encode password policy configuration
     * @param dataSource                                the data source
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration,
                                                                 final DataSource dataSource) {
        val databasePasswordEncoder = new QueryAndEncodeDatabasePasswordEncoder(properties);
        val handler = new QueryAndEncodeDatabaseAuthenticationHandler(properties, servicesManager,
            jdbcPrincipalFactory, dataSource, databasePasswordEncoder);
        configureJdbcAuthenticationHandler(handler, queryAndEncodePasswordPolicyConfiguration, properties, applicationContext);
        return handler;
    }

    /**
     * New authentication handler.
     *
     * @param properties                       the properties
     * @param applicationContext               the application context
     * @param jdbcPrincipalFactory             the jdbc principal factory
     * @param servicesManager                  the services manager
     * @param queryPasswordPolicyConfiguration the query password policy configuration
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final QueryJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext queryPasswordPolicyConfiguration) {

        val handler = new QueryDatabaseAuthenticationHandler(properties, servicesManager, jdbcPrincipalFactory, JpaBeans.newDataSource(properties));
        configureJdbcAuthenticationHandler(handler, queryPasswordPolicyConfiguration, properties, applicationContext);
        return handler;
    }

    /**
     * New authentication handler.
     *
     * @param properties                            the properties
     * @param applicationContext                    the application context
     * @param jdbcPrincipalFactory                  the jdbc principal factory
     * @param servicesManager                       the services manager
     * @param searchModePasswordPolicyConfiguration the search mode password policy configuration
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final SearchJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext searchModePasswordPolicyConfiguration) {
        val handler = new SearchModeSearchDatabaseAuthenticationHandler(properties, servicesManager,
            jdbcPrincipalFactory, JpaBeans.newDataSource(properties));
        configureJdbcAuthenticationHandler(handler, searchModePasswordPolicyConfiguration, properties, applicationContext);
        return handler;
    }

    /**
     * New authentication handler.
     *
     * @param properties                            the properties
     * @param applicationContext                    the application context
     * @param jdbcPrincipalFactory                  the jdbc principal factory
     * @param servicesManager                       the services manager
     * @param searchModePasswordPolicyConfiguration the search mode password policy configuration
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final ProcedureJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext searchModePasswordPolicyConfiguration) {
        val handler = new StoredProcedureAuthenticationHandler(properties, servicesManager,
            jdbcPrincipalFactory, JpaBeans.newDataSource(properties));
        configureJdbcAuthenticationHandler(handler, searchModePasswordPolicyConfiguration, properties, applicationContext);
        return handler;
    }

    /**
     * New authentication handler.
     *
     * @param properties            the properties
     * @param applicationContext    the application context
     * @param jdbcPrincipalFactory  the jdbc principal factory
     * @param servicesManager       the services manager
     * @param passwordPolicyContext the search mode password policy configuration
     * @return the authentication handler
     */
    public static AuthenticationHandler newAuthenticationHandler(final BaseJdbcAuthenticationProperties properties,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final PrincipalFactory jdbcPrincipalFactory,
                                                                 final ServicesManager servicesManager,
                                                                 final PasswordPolicyContext passwordPolicyContext) {
        return switch (properties) {
            case QueryJdbcAuthenticationProperties query -> newAuthenticationHandler(query, applicationContext, jdbcPrincipalFactory,
                servicesManager, passwordPolicyContext);
            case QueryEncodeJdbcAuthenticationProperties queryEncode -> newAuthenticationHandler(queryEncode, applicationContext, jdbcPrincipalFactory,
                servicesManager, passwordPolicyContext);
            case ProcedureJdbcAuthenticationProperties procedure -> newAuthenticationHandler(procedure, applicationContext, jdbcPrincipalFactory,
                servicesManager, passwordPolicyContext);
            case SearchJdbcAuthenticationProperties search -> newAuthenticationHandler(search, applicationContext, jdbcPrincipalFactory,
                servicesManager, passwordPolicyContext);
            case BindJdbcAuthenticationProperties bind -> newAuthenticationHandler(bind, applicationContext, jdbcPrincipalFactory,
                servicesManager, passwordPolicyContext);
            default -> throw new IllegalStateException("Unexpected value: " + properties.getClass().getName());
        };
    }
}
