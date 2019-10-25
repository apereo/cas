package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This is {@link OAuth20UserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface OAuth20UserProfileViewRenderer {

    /**
     * Attribute in the model that represents the id.
     */
    String MODEL_ATTRIBUTE_ID = "id";

    /**
     * Attribute in the model that represents the client id.
     */
    String MODEL_ATTRIBUTE_CLIENT_ID = OAuth20Constants.CLIENT_ID;

    /**
     * Attribute in the model that represents collection of attributes.
     */
    String MODEL_ATTRIBUTE_ATTRIBUTES = "attributes";

    /**
     * Render.
     *
     * @param model       the model
     * @param accessToken the access token
     * @param response    the response
     * @return the string
     */
    ResponseEntity render(Map<String, Object> model, OAuth20AccessToken accessToken, HttpServletResponse response);
}
