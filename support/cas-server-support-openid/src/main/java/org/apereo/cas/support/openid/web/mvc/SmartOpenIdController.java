package org.apereo.cas.support.openid.web.mvc;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.AbstractDelegateController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates an association to an openid association request.
 *
 * @author Frederic Esnault
 * @since 3.5
 * @deprecated 6.2
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "6.2.0")
public class SmartOpenIdController extends AbstractDelegateController implements Serializable {
    private static final long serialVersionUID = -594058549445950430L;

    private final transient ServerManager serverManager;

    private final transient View successView;

    /**
     * Gets the association response. Determines the mode first.
     * If mode is set to associate, will set the response. Then
     * builds the response parameters next and returns.
     *
     * @param request the request
     * @return the association response
     */
    public Map<String, String> getAssociationResponse(final HttpServletRequest request) {
        val parameters = new ParameterList(request.getParameterMap());

        val mode = parameters.hasParameter(OpenIdProtocolConstants.OPENID_MODE)
            ? parameters.getParameterValue(OpenIdProtocolConstants.OPENID_MODE)
            : null;

        val response = FunctionUtils.doIf(StringUtils.equals(mode, OpenIdProtocolConstants.ASSOCIATE),
            () -> this.serverManager.associationResponse(parameters),
            () -> null)
            .get();

        val responseParams = new HashMap<String, String>();
        if (response != null) {
            responseParams.putAll(response.getParameterMap());
        }
        return responseParams;
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        val openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.ASSOCIATE)) {
            LOGGER.info("Handling request. openid.mode : [{}]", openIdMode);
            return true;
        }
        LOGGER.info("Cannot handle request. openid.mode : [{}]", openIdMode);
        return false;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        val parameters = new HashMap<String, String>(getAssociationResponse(request));
        return new ModelAndView(this.successView, parameters);
    }
}
