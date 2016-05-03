package org.jasig.cas.web;

import org.jasig.cas.OidcConstants;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.web.OAuth20ProfileController;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is {@link OidcProfileController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Controller("oidcProfileController")
public class OidcProfileController extends OAuth20ProfileController {

    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.PROFILE_URL,
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    protected ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                           final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected LinkedMultiValueMap<String, String> writeOutProfileResponse(final Principal principal) throws IOException {
        final LinkedMultiValueMap<String, String> map = super.writeOutProfileResponse(principal);
        if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
            map.add(OidcConstants.CLAIM_SUB, principal.getId());
        }
        return map;
    }
}
