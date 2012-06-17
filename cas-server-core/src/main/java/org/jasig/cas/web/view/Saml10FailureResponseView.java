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
package org.jasig.cas.web.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.web.support.SamlArgumentExtractor;
import org.opensaml.SAMLException;
import org.opensaml.SAMLResponse;

/**
 * Represents a failed attempt at validating a ticket, responding via a SAML
 * assertion.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class Saml10FailureResponseView extends AbstractCasView {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private final SamlArgumentExtractor samlArgumentExtractor = new SamlArgumentExtractor();

    @NotNull
    private String encoding = DEFAULT_ENCODING;

    protected void renderMergedOutputModel(final Map model,
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final WebApplicationService service = this.samlArgumentExtractor.extractService(request);
        final String artifactId = service != null ? service.getArtifactId() : null;
        final String serviceId = service != null ? service.getId() : "UNKNOWN";
        final String errorMessage = (String) model.get("description");
        final SAMLResponse samlResponse = new SAMLResponse(artifactId, serviceId, new ArrayList<Object>(), new SAMLException(errorMessage));
        samlResponse.setIssueInstant(new Date());

        response.setContentType("text/xml; charset=" + this.encoding);
        response.getWriter().print("<?xml version=\"1.0\" encoding=\"" + this.encoding + "\"?>");
        response.getWriter().print("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body>");
        response.getWriter().print(samlResponse.toString());
        response.getWriter().print("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }
}
