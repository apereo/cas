package org.jasig.cas.support.openid.web.mvc;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.web.DelegateController;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * @author Frederic Esnault
 * @version $Id$
 * @since 3.5
 */
public class SmartOpenIdController extends DelegateController implements Serializable {

    private static final long serialVersionUID = -594058549445950430L;

    private Logger logger = LoggerFactory.getLogger(SmartOpenIdController.class);

    private ServerManager serverManager;

    /** View if association Fails. */
    private static final String DEFAULT_ASSOCIATION_FAILURE_VIEW_NAME = "casOpenIdAssociationFailureView";

    /** View if association Succeeds. */
    private static final String DEFAULT_ASSOCIATION_SUCCESS_VIEW_NAME = "casOpenIdAssociationSuccessView";

    /** The view to redirect to on a successful validation. */
    @NotNull
    private String successView = DEFAULT_ASSOCIATION_SUCCESS_VIEW_NAME;

    /** The view to redirect to on a validation failure. */
    @NotNull
    private String failureView = DEFAULT_ASSOCIATION_FAILURE_VIEW_NAME;

    /** Constant representing the Assertion in the model. */
    private static final String MODEL_ASSERTION = "assertion";

    private CentralAuthenticationService centralAuthenticationService;


    public Map<String, String> getAssociationResponse(HttpServletRequest request) {
        ParameterList parameters = new ParameterList(request.getParameterMap());

        String mode = parameters.hasParameter("openid.mode") ?
                parameters.getParameterValue("openid.mode") : null;
        Message response = null;
        if (mode != null && mode.equals("associate")) {
            response = serverManager.associationResponse(parameters);
        }
        final Map<String, String> responseParams = new HashMap<String, String>();
        if (response != null) {
            responseParams.putAll(response.getParameterMap());
        }

        return responseParams;

    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.putAll(getAssociationResponse(request));
        return new ModelAndView(successView, "parameters", parameters);
    }

    @Override
    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {
        String openIdMode = request.getParameter("openid.mode");
        if (openIdMode != null && openIdMode.equals("associate")) {
            logger.info("Handling request. openid.mode : "+openIdMode);
            return true;
        }
        logger.info("Cannot handle request. openid.mode : "+openIdMode);
        return false;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setFailureView(String failureView) {
        this.failureView = failureView;
    }

    @NotNull
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @NotNull
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
