package org.jasig.cas.config;

import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.IpClient;
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSecurityContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casSecurityContextConfiguration")
public class CasSecurityContextConfiguration {

    /**
     * The Regex pattern.
     */
    @Value("${cas.securityContext.adminpages.ip:127\\.0\\.\\.1}")
    private String regexPattern;

    /**
     * Requires authentication interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name = "requiresAuthenticationInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationInterceptor() {
        return new RequiresAuthenticationInterceptor(new Config(new IpClient(new IpRegexpAuthenticator(this.regexPattern))), "IpClient");
    }
}
