package org.apereo.cas.adaptors.jdbc.config;

import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
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

    @Autowired(required = false)
    @Qualifier("searchModeDatabaseDataSource")
    private DataSource searchModeDatabaseDataSource;

    @Autowired(required = false)
    @Qualifier("queryEncodeDatabaseDataSource")
    private DataSource queryEncodeDatabaseDataSource;


    @Autowired(required = false)
    @Qualifier("bindSearchDatabaseDataSource")
    private DataSource bindSearchDatabaseDataSource;

    @Autowired(required = false)
    @Qualifier("queryDatabaseDataSource")
    private DataSource queryDatabaseDataSource;

    @Autowired
    private JdbcAuthenticationProperties properties;

    @Bean
    @RefreshScope
    public AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler() {
        final BindModeSearchDatabaseAuthenticationHandler h =
                new BindModeSearchDatabaseAuthenticationHandler();
        h.setDataSource(this.bindSearchDatabaseDataSource);
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler() {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();

        q.setAlgorithmName(properties.getEncode().getAlgorithmName());
        q.setNumberOfIterationsFieldName(properties.getEncode().getNumberOfIterationsFieldName());
        q.setNumberOfIterations(properties.getEncode().getNumberOfIterations());
        q.setPasswordFieldName(properties.getEncode().getPasswordFieldName());
        q.setSaltFieldName(properties.getEncode().getSaltFieldName());
        q.setSql(properties.getEncode().getSql());
        q.setStaticSalt(properties.getEncode().getStaticSalt());
        q.setDataSource(queryEncodeDatabaseDataSource);

        return q;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryDatabaseAuthenticationHandler() {
        final QueryDatabaseAuthenticationHandler h =
                new QueryDatabaseAuthenticationHandler();
        h.setDataSource(queryDatabaseDataSource);
        h.setSql(properties.getQuery().getSql());
        return h;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler() {
        final SearchModeSearchDatabaseAuthenticationHandler a = new SearchModeSearchDatabaseAuthenticationHandler();

        a.setDataSource(searchModeDatabaseDataSource);
        a.setFieldPassword(properties.getSearch().getFieldPassword());
        a.setFieldUser(properties.getSearch().getFieldUser());
        a.setTableUsers(properties.getSearch().getTableUsers());

        return a;
    }
}
