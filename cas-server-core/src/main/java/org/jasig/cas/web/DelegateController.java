/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class to be extended by all controllers that may become a delegate.
 * All subclass must implement the canHandle method to say if they can handle a request or not.
 * @author Frederic Esnault
 * @since 3.5
 * @deprecated As of 4.1, the class is required to note its abstractness in the name and will be renamed in the future.
 */
@Deprecated
public abstract class DelegateController extends AbstractController {
    
    /** The logger. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Determine if a DelegateController subclass can handle the current request.
     * @param request the current request
     * @param response the response
     * @return true if the controller can handler the request, false otherwise
     */
    public abstract boolean canHandle(HttpServletRequest request, HttpServletResponse response);


}
