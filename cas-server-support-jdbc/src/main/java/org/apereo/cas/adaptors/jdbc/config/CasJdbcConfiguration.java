package org.apereo.cas.adaptors.jdbc.config;

import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcConfiguration")
public class CasJdbcConfiguration {

    @Autowired(required=false)
    @Qualifier("queryAndEncodePasswordEncoder")
    private PasswordEncoder queryAndEncodePasswordEncoder;

    @Autowired(required=false)
    @Qualifier("queryAndEncodePrincipalNameTransformer")
    private PrincipalNameTransformer queryAndEncodePrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("queryAndEncodePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration queryAndEncodePasswordPolicyConfiguration;
    
    @Autowired(required=false)
    @Qualifier("searchModePasswordEncoder")
    private PasswordEncoder searchModePasswordEncoder;

    @Autowired(required=false)
    @Qualifier("searchModePrincipalNameTransformer")
    private PrincipalNameTransformer searchModePrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("searchModePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration searchModePasswordPolicyConfiguration;
    
    @Autowired(required=false)
    @Qualifier("queryPasswordEncoder")
    private PasswordEncoder queryPasswordEncoder;

    @Autowired(required=false)
    @Qualifier("queryPrincipalNameTransformer")
    private PrincipalNameTransformer queryPrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("queryPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration queryPasswordPolicyConfiguration;
    
    @Autowired(required = false)
    @Qualifier("searchModeDatabaseDataSource")
    private DataSource searchModeDatabaseDataSource;

    @Autowired(required = false)
    @Qualifier("queryEncodeDatabaseDataSource")
    private DataSource queryEncodeDatabaseDataSource;
    
    @Autowired(required = false)
    @Qualifier("bindSearchDatabaseDataSource")
    private DataSource bindSearchDatabaseDataSource;
    
    @Autowired(required=false)
    @Qualifier("bindSearchPasswordEncoder")
    private PasswordEncoder bindSearchPasswordEncoder;

    @Autowired(required=false)
    @Qualifier("bindSearchPrincipalNameTransformer")
    private PrincipalNameTransformer bindSearchPrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("bindSearchPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration bindSearchPasswordPolicyConfiguration;
    
    @Autowired(required = false)
    @Qualifier("queryDatabaseDataSource")
    private DataSource queryDatabaseDataSource;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler() {
        final BindModeSearchDatabaseAuthenticationHandler h =
                new BindModeSearchDatabaseAuthenticationHandler();
        h.setDataSource(this.bindSearchDatabaseDataSource);

        if (bindSearchPasswordEncoder != null) {
            h.setPasswordEncoder(bindSearchPasswordEncoder);
        }
        if (bindSearchPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(bindSearchPasswordPolicyConfiguration);
        }
        if (bindSearchPrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(bindSearchPrincipalNameTransformer);
        }
        
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler() {
        final QueryAndEncodeDatabaseAuthenticationHandler h = new QueryAndEncodeDatabaseAuthenticationHandler();

        h.setAlgorithmName(casProperties.getJdbcAuthn().getEncode().getAlgorithmName());
        h.setNumberOfIterationsFieldName(casProperties.getJdbcAuthn().getEncode().getNumberOfIterationsFieldName());
        h.setNumberOfIterations(casProperties.getJdbcAuthn().getEncode().getNumberOfIterations());
        h.setPasswordFieldName(casProperties.getJdbcAuthn().getEncode().getPasswordFieldName());
        h.setSaltFieldName(casProperties.getJdbcAuthn().getEncode().getSaltFieldName());
        h.setSql(casProperties.getJdbcAuthn().getEncode().getSql());
        h.setStaticSalt(casProperties.getJdbcAuthn().getEncode().getStaticSalt());
        h.setDataSource(queryEncodeDatabaseDataSource);

        if (queryAndEncodePasswordEncoder != null) {
            h.setPasswordEncoder(queryAndEncodePasswordEncoder);
        }
        if (queryAndEncodePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(queryAndEncodePasswordPolicyConfiguration);
        }
        if (queryAndEncodePrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(queryAndEncodePrincipalNameTransformer);
        }
        
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryDatabaseAuthenticationHandler() {
        final QueryDatabaseAuthenticationHandler h =
                new QueryDatabaseAuthenticationHandler();
        h.setDataSource(queryDatabaseDataSource);
        h.setSql(casProperties.getJdbcAuthn().getQuery().getSql());

        if (queryPasswordEncoder != null) {
            h.setPasswordEncoder(queryPasswordEncoder);
        }
        if (queryPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(queryPasswordPolicyConfiguration);
        }
        if (queryPrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(queryPrincipalNameTransformer);
        }
        
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler() {
        final SearchModeSearchDatabaseAuthenticationHandler h = new SearchModeSearchDatabaseAuthenticationHandler();

        h.setDataSource(searchModeDatabaseDataSource);
        h.setFieldPassword(casProperties.getJdbcAuthn().getSearch().getFieldPassword());
        h.setFieldUser(casProperties.getJdbcAuthn().getSearch().getFieldUser());
        h.setTableUsers(casProperties.getJdbcAuthn().getSearch().getTableUsers());

        if (searchModePasswordEncoder != null) {
            h.setPasswordEncoder(searchModePasswordEncoder);
        }
        if (searchModePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(searchModePasswordPolicyConfiguration);
        }
        if (searchModePrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(searchModePrincipalNameTransformer);
        }
        
        return h;
    }
}
