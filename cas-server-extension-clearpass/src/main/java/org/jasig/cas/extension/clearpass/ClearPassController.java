/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.extension.clearpass;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that returns the password based on some external authentication/authorization rules.  The recommended
 * method is to use the Jasig CAS Client for Java and its proxy authentication features.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0
 */
public final class ClearPassController extends AbstractController {

    private final static Logger log = LoggerFactory.getLogger(ClearPassController.class);
    
	// view if clearpass request fails
    private static final String DEFAULT_SERVICE_FAILURE_VIEW_NAME = "protocol/clearPass/clearPassFailure";

    // view if clearpass request succeeds
    private static final String DEFAULT_SERVICE_SUCCESS_VIEW_NAME = "protocol/clearPass/clearPassSuccess";
    
    // key under which clearpass will be placed into the model
    private static final String MODEL_CLEARPASS = "credentials";
    
    // key under which failure descriptions are placed into the model
    private static final String MODEL_FAILURE_DESCRIPTION = "description";

    @NotNull
    private String successView = DEFAULT_SERVICE_SUCCESS_VIEW_NAME;

    @NotNull
    private String failureView = DEFAULT_SERVICE_FAILURE_VIEW_NAME;
    
    @NotNull
    private final Map<String, String> credentialsCache;

    public ClearPassController(final Map<String, String> credentialsCache) {
        this.credentialsCache = credentialsCache;
    }

    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String userName = request.getRemoteUser();

        log.debug("Handling clearPass request for user " + userName);

        if (userName != null) {
            final String password = this.credentialsCache.get(userName);
            return new ModelAndView(this.successView, MODEL_CLEARPASS, password);
        }

        return returnError("No authentication information provided.");
    }
    
    protected ModelAndView returnError(String description) {
        ModelAndView mv=new ModelAndView(this.failureView);
        mv.addObject(MODEL_FAILURE_DESCRIPTION, description);
        return(mv);
    }

    public void setSuccessView(final String successView) {
		this.successView = successView;
	}

	public void setFailureView(final String failureView) {
		this.failureView = failureView;
	}
}
