package org.apereo.cas.adaptors.jdbc.config;

import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcConfiguration")
public class CasJdbcConfiguration {
    
    @Bean
    @RefreshScope
    public AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler() {
        return new BindModeSearchDatabaseAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler() {
        return new QueryAndEncodeDatabaseAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler queryDatabaseAuthenticationHandler() {
        return new QueryDatabaseAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler() {
        return new SearchModeSearchDatabaseAuthenticationHandler();
    }
}
