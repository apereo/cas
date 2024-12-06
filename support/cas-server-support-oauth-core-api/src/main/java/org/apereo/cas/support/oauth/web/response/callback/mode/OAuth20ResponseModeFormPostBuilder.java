package org.apereo.cas.support.oauth.web.response.callback.mode;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ResponseModeFormPostBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class OAuth20ResponseModeFormPostBuilder implements OAuth20ResponseModeBuilder {
    @Override
    public ModelAndView build(final RegisteredService registeredService,
                              final String redirectUrl, final Map<String, String> parameters) {
        val model = new LinkedHashMap<String, Object>();
        model.put("originalUrl", redirectUrl);
        model.put("parameters", parameters);
        val mv = new ModelAndView(CasWebflowConstants.VIEW_ID_POST_RESPONSE, model);
        mv.setStatus(HttpStatusCode.valueOf(HttpStatus.OK.value()));
        LOGGER.debug("POSTing to [{}] with model [{}]", mv.getViewName(), mv.getModel());
        return mv;
    }

    @Override
    public OAuth20ResponseModeTypes getResponseMode() {
        return OAuth20ResponseModeTypes.FORM_POST;
    }
}
