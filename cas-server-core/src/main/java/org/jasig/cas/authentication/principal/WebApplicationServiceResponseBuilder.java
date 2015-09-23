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

package org.jasig.cas.authentication.principal;

import org.jasig.cas.CasProtocolConstants;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Default response builder that passes back the ticket
 * id to the original url of the service based on the response type.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WebApplicationServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {
    private final Response.ResponseType responseType;

    /**
     * Instantiates a new Web application service response builder.
     * @param type the type
     */
    public WebApplicationServiceResponseBuilder(final Response.ResponseType type) {
        this.responseType = type;
    }

    @Override
    public Response build(final WebApplicationService service, final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.hasText(ticketId)) {
            parameters.put(CasProtocolConstants.PARAMETER_TICKET, ticketId);
        }

        if (responseType.equals(Response.ResponseType.POST)) {
            return buildPost(service, parameters);
        }
        if (responseType.equals(Response.ResponseType.REDIRECT)) {
            return buildRedirect(service, parameters);
        }

        throw new IllegalArgumentException("Response type is valid. Only POST/REDIRECT are supported");
    }



}
