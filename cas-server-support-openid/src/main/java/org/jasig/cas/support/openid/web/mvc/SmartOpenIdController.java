package org.jasig.cas.support.openid.web.mvc;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.web.AbstractDelegateController;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates an association to an openid association request.
 * @author Frederic Esnault
 * @since 3.5
 */
@RefreshScope
@Component("smartOpenIdAssociationController")
public class SmartOpenIdController extends AbstractDelegateController implements Serializable {

    private static final long serialVersionUID = -594058549445950430L;

    /** View if association Fails. */
    private static final String DEFAULT_ASSOCIATION_FAILURE_VIEW_NAME = "casOpenIdAssociationFailureView";

    /** View if association Succeeds. */
    private static final String DEFAULT_ASSOCIATION_SUCCESS_VIEW_NAME = "casOpenIdAssociationSuccessView";

    private transient Logger logger = LoggerFactory.getLogger(SmartOpenIdController.class);

    @Autowired
    @Qualifier("serverManager")
    private ServerManager serverManager;

    /** The view to redirect to on a successful validation. */
    
    private String successView = DEFAULT_ASSOCIATION_SUCCESS_VIEW_NAME;

    /** The view to redirect to on a validation failure. Not used for now,
     *  the succes view may return failed association attemps. No need for another view. */
    
    private String failureView = DEFAULT_ASSOCIATION_FAILURE_VIEW_NAME;

    /**
     * Gets the association response. Determines the mode first.
     * If mode is set to associate, will set the response. Then
     * builds the response parameters next and returns.
     *
     * @param request the request
     * @return the association response
     */
    public Map<String, String> getAssociationResponse(final HttpServletRequest request) {
        final ParameterList parameters = new ParameterList(request.getParameterMap());

        final String mode = parameters.hasParameter(OpenIdProtocolConstants.OPENID_MODE)
                ? parameters.getParameterValue(OpenIdProtocolConstants.OPENID_MODE)
                : null;

        Message response = null;

        if (StringUtils.equals(mode, OpenIdProtocolConstants.ASSOCIATE)) {
            response = serverManager.associationResponse(parameters);
        }
        final Map<String, String> responseParams = new HashMap<>();
        if (response != null) {
            responseParams.putAll(response.getParameterMap());
        }

        return responseParams;

    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.putAll(getAssociationResponse(request));
        return new ModelAndView(successView, "parameters", parameters);
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        final String openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.ASSOCIATE)) {
            logger.info("Handling request. openid.mode : {}", openIdMode);
            return true;
        }
        logger.info("Cannot handle request. openid.mode : {}", openIdMode);
        return false;
    }

    public void setSuccessView(final String successView) {
        this.successView = successView;
    }

    public void setFailureView(final String failureView) {
        this.failureView = failureView;
    }

    public void setServerManager(final ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
