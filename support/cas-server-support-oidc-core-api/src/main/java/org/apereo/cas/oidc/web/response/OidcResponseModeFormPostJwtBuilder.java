package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OidcResponseModeFormPostJwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class OidcResponseModeFormPostJwtBuilder extends BaseOAuth20JwtResponseModeBuilder {

    public OidcResponseModeFormPostJwtBuilder(final ObjectProvider<@NonNull OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl,
                              final Map<String, String> parameters) {
        return configurationContext
            .stream()
            .map(Unchecked.function(ctx -> {
                val token = buildJwtResponse(registeredService, parameters);
                val model = new LinkedHashMap<String, Object>();
                model.put("originalUrl", redirectUrl);
                model.put("parameters", Map.of("response", token));
                val mv = new ModelAndView(CasWebflowConstants.VIEW_ID_POST_RESPONSE, model);
                mv.setStatus(HttpStatusCode.valueOf(HttpStatus.OK.value()));
                LOGGER.debug("Redirecting to [{}] with model [{}]", mv.getViewName(), mv.getModel());
                return mv;
            }))
            .findFirst()
            .orElseThrow();
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.FORM_POST_JWT;
    }
}
