package org.apereo.cas.config;

import org.apereo.cas.support.oauth.OAuthConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * This this {@link OAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oauthConfiguration")
public class OAuthConfiguration extends WebMvcConfigurerAdapter {

    private static final String CAS_OAUTH_CLIENT = "CasOAuthClient";

    @Autowired
    @Qualifier("oAuthUserAuthenticator")
    private UsernamePasswordAuthenticator oAuthUserAuthenticator;

    @Autowired
    @Qualifier("oAuthClientAuthenticator")
    private UsernamePasswordAuthenticator oAuthClientAuthenticator;

    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String casLoginUrl;

    @Value("${server.prefix:http://localhost:8080/cas}/oauth2.0/callbackAuthorize")
    private String callbackUrl;
    
    /**
     * Oauth sec config config.
     *
     * @return the config
     */
    @RefreshScope
    @Bean(name = "oauthSecConfig")
    public Config oauthSecConfig() {
        final CasClient oauthCasClient = new CasClient(this.casLoginUrl);
        oauthCasClient.setName(CAS_OAUTH_CLIENT);

        final DirectBasicAuthClient basicAuthClient = new DirectBasicAuthClient(this.oAuthClientAuthenticator);
        basicAuthClient.setName("clientBasicAuth");

        final DirectFormClient directFormClient = new DirectFormClient(this.oAuthClientAuthenticator);
        directFormClient.setName("clientForm");
        directFormClient.setUsernameParameter("client_id");
        directFormClient.setPasswordParameter("client_secret");

        final DirectFormClient userFormClient = new DirectFormClient(this.oAuthUserAuthenticator);
        userFormClient.setName("userForm");

        return new Config(this.callbackUrl, oauthCasClient, basicAuthClient, directFormClient, userFormClient);
    }

    /**
     * Requires authentication authorize interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @RefreshScope
    @Bean(name = "requiresAuthenticationAuthorizeInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), CAS_OAUTH_CLIENT);
    }
    
    /**
     * Requires authentication access token interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @RefreshScope
    @Bean(name = "requiresAuthenticationAccessTokenInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAccessTokenInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), "clientBasicAuth,clientForm,userForm");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(requiresAuthenticationAuthorizeInterceptor())
                .addPathPatterns(OAuthConstants.BASE_OAUTH20_URL.concat("/").concat(OAuthConstants.AUTHORIZE_URL).concat("*"));

        registry.addInterceptor(requiresAuthenticationAccessTokenInterceptor())
                .addPathPatterns(OAuthConstants.BASE_OAUTH20_URL.concat("/").concat(OAuthConstants.ACCESS_TOKEN_URL).concat("*"));
    }
}
