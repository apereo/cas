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

package org.jasig.cas.support.saml.web.idp.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link SSOProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller
@RequestMapping("/idp/profile/SAML2/Redirect/SSO")
public class SSOProfileHandlerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SSOProfileHandlerController() {
    }

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    public void postConstruct() {}

    @RequestMapping(method = RequestMethod.GET)
    protected void handleProfileRequest(final HttpServletResponse response, final HttpServletRequest request) throws IOException {
        logger.info("Received profile request {}", request.getRequestURI());
    }
}
