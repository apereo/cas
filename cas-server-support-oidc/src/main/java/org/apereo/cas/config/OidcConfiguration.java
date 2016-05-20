package org.apereo.cas.config;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
public class OidcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("oidcAuthorizationRequestSupport")
    private OidcAuthorizationRequestSupport oidcAuthzRequestSupport;

    @Autowired
    @Qualifier("oauthInterceptor")
    private HandlerInterceptor oauthInterceptor;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oidcInterceptor())
                .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL.concat("/").concat("*"));
    }

    /**
     * Callback authorize view resolver.
     *
     * @return the oauth 20 callback authorize view resolver
     */
    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean(name="callbackAuthorizeViewResolver")
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
            @Override
            public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
                final Set<String> prompts = oidcAuthzRequestSupport.getOidcPromptFromAuthorizationRequest(url);
                if (prompts.contains(OidcConstants.PROMPT_NONE)) {
                    if (manager.get(true) != null) {
                        return new ModelAndView(url);
                    }
                    final Map<String, String> model = new HashMap<>();
                    model.put(OAuthConstants.ERROR, OidcConstants.LOGIN_REQUIRED);
                    return new ModelAndView(new MappingJackson2JsonView(), model);
                }
                return new ModelAndView(new RedirectView(url));
            }
        };
    }
    
    /**
     * Oidc interceptor handler interceptor.
     *
     * @return the handler interceptor
     */
    @Bean(name = "oidcInterceptor")
    public HandlerInterceptor oidcInterceptor() {
        return this.oauthInterceptor;
    }

    /**
     * Oauth cas client redirect action builder.
     *
     * @return the o auth cas client redirect action builder
     */
    @Bean(name = "oauthCasClientRedirectActionBuilder", autowire = Autowire.BY_NAME)
    public OAuthCasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder();
    }

    /**
     * Requires authentication authorize interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean(name = "requiresAuthenticationAuthorizeInterceptor")
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        final String name = oauthSecConfig.getClients().findClient(CasClient.class).getName();
        return new RequiresAuthenticationInterceptor(oauthSecConfig, name) {
            
            @Override
            public boolean preHandle(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final Object handler) throws Exception {
                final J2EContext ctx = new J2EContext(request, response);
                final ProfileManager manager = new ProfileManager(ctx);
                
                boolean clearCreds = false;
                final Optional<UserProfile> auth = oidcAuthzRequestSupport.isAuthenticationProfileAvailable(ctx);

                if (auth.isPresent()) {
                    final Optional<Long> maxAge = oidcAuthzRequestSupport.getOidcMaxAgeFromAuthorizationRequest(ctx);
                    if (maxAge.isPresent()) {
                        clearCreds = oidcAuthzRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(ctx, auth.get());
                    }
                }


                final Set<String> prompts = oidcAuthzRequestSupport.getOidcPromptFromAuthorizationRequest(ctx);
                if (!clearCreds) {
                    clearCreds = prompts.contains(OidcConstants.PROMPT_LOGIN);
                }

                if (clearCreds) {
                    clearCreds = !prompts.contains(OidcConstants.PROMPT_NONE);
                }
                
                if (clearCreds) {
                    manager.remove(true);
                }
                return super.preHandle(request, response, handler);
            }
        };
    }
}













