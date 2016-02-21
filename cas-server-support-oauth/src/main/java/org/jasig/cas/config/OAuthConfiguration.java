package org.jasig.cas.config;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This this {@link OAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("oauthConfiguration")
public class OAuthConfiguration {

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
    @Bean(name="oauthConfirmView")
    public JstlView oauthConfirmView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/oauth/confirm.jsp");    
    }

    /**
     * Oauth sec config config.
     *
     * @return the config
     */
    @Bean(name="oauthSecConfig")
    public Config oauthSecConfig() {
        final CasClient client = new CasClient(this.casLoginUrl);
        client.setName("CasOAuthClient");
        return new Config(this.callbackUrl, client);
    }


    /**
     * Requires authentication authorize interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name="requiresAuthenticationAuthorizeInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), 
                "CASOAuthClient");
    }
}
