package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * This is {@link OidcNoOpClientConfigurationEndpointController}.
 * Acts as a disabled placeholder when dynamic client registration is turned off.
 *
 * @author Jiří Prokop
 * @since 7.3.0
 */
@Slf4j
public class NoOpOidcInitialAccessTokenController extends OidcInitialAccessTokenController {
    public NoOpOidcInitialAccessTokenController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public ModelAndView handleRequestInternal(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) {
        LOGGER.debug("OIDC Initial Access Token endpoint is disabled.");
        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.NOT_IMPLEMENTED);
        return mv;
    }
}
