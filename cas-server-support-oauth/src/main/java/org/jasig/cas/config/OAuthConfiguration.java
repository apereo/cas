package org.jasig.cas.config;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.JstlView;

/**
 * This this {@link OAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("oauthConfiguration")
@ComponentScan(basePackages = {"org.pac4j.springframework"})
@Lazy(true)
public class OAuthConfiguration extends WebMvcConfigurerAdapter {

    /**
     * The Oauth client authenticator.
     */
    @Autowired
    @Qualifier("oAuthUserAuthenticator")
    private UsernamePasswordAuthenticator oAuthUserAuthenticator;

    @Autowired
    @Qualifier("oAuthClientAuthenticator")
    private UsernamePasswordAuthenticator oAuthClientAuthenticator;


    /**
     * The Cas login url.
     */
    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String casLoginUrl;

    /**
     * The Callback url.
     */
    @Value("${server.prefix:http://localhost:8080/cas}/oauth2.0/callbackAuthorize")
    private String callbackUrl;

    /**
     * Oauth confirm view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "oauthConfirmView")
    public JstlView oauthConfirmView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/oauth/confirm.jsp");
    }

    /**
     * Oauth sec config config.
     *
     * @return the config
     */
    @Bean(name = "oauthSecConfig")
    public Config oauthSecConfig() {
        final CasClient oauthCasClient = new CasClient(this.casLoginUrl);
        oauthCasClient.setName("CasOAuthClient");

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
    @Bean(name = "requiresAuthenticationAuthorizeInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), "CasOAuthClient");
    }

    /**
     * Requires authentication access token interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name = "requiresAuthenticationAccessTokenInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAccessTokenInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), "clientBasicAuth,clientForm,userForm");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(requiresAuthenticationAuthorizeInterceptor())
                .addPathPatterns("/oauth2.0/authorize*");

        registry.addInterceptor(requiresAuthenticationAccessTokenInterceptor())
                .addPathPatterns("/oauth2.0/accessToken*");
    }
}
