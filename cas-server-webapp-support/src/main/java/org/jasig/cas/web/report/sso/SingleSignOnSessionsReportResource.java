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

package org.jasig.cas.web.report.sso;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * RESTful HTTP resource to expose <code>SingleSignOnSessionsReport</code>
 * as <i>application/json</i> media type.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component
@Path("/")
public class SingleSignOnSessionsReportResource {

    private static final String ROOT_REPORT_ACTIVE_SESSIONS_KEY = "activeSsoSessions";

    private static final String ROOT_REPORT_NA_KEY = "notAvailable";

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleSignOnSessionsReportResource.class);

    private final SingleSignOnSessionsReport singleSignOnSessionsReport;

    private final ObjectMapper jsonMapper = new ObjectMapper();


    /**
     * Instantiates a new Single sign on sessions report resource.
     *
     * @param singleSignOnSessionsReport the single sign on sessions report
     */
    @Autowired
    public SingleSignOnSessionsReportResource(final SingleSignOnSessionsReport singleSignOnSessionsReport) {
        this.singleSignOnSessionsReport = singleSignOnSessionsReport;
        //Configure mapper strategies
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Show active sso sessions.
     *
     * @return the response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response showActiveSsoSessions() {
        final Map<String, Object> sessionsMap = new HashMap<String, Object>(1);
        Collection<Map<String, Object>> activeSessions = null;
        String jsonRepresentation = null;

        try {
            activeSessions = this.singleSignOnSessionsReport.getActiveSsoSessions();
            sessionsMap.put(ROOT_REPORT_ACTIVE_SESSIONS_KEY, activeSessions);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e.getCause());
            sessionsMap.put(ROOT_REPORT_NA_KEY, e.getMessage());
        }

        try {
            jsonRepresentation = this.jsonMapper.writeValueAsString(sessionsMap);
        } catch (final JsonProcessingException e) {
            LOGGER.error("An exception has been caught during an attempt to serialize <active sso sessions report>", e);
            return Response.serverError().build();
        }
        return Response.ok(jsonRepresentation).build();
    }
}
