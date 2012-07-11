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
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

/**
 * Provides special handling for parameters in requests made to the CAS login webflow.
 * In particular, it drops the execution parameter from the flow execution URL and always
 * appends query string parameters to flow URLs.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4
 */
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final String url = request.getRequestURI();
        // Append querystring parameters from original request if present
        final String qs = request.getQueryString();
        if (qs != null) {
            final StringBuilder builder = new StringBuilder(url);
            builder.append('?');
            builder.append(request.getQueryString());
            return builder.toString();
        }
        return url;
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        final String url = super.createFlowDefinitionUrl(flowId, input, request);
        // Append querystring parameters from original request if present
        final String qs = request.getQueryString();
        if (qs != null) {
            final StringBuilder builder = new StringBuilder(request.getRequestURI());
            builder.append('?').append(qs);
            return builder.toString();
        }
        return url;
    }
}
