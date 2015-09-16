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
package org.jasig.cas.login;

import java.io.IOException;

import com.meterware.httpunit.WebResponse;

/**
 * Helper class for accomplishing CAS login, a common task of compatibility tests.
 * @author Marvin Addison
 * @since 3.0.0
 */
public final class LoginHelper {

    /**
     * Instantiates a new login helper.
     */
    private LoginHelper() {}

    /**
     * Extract a serviceTicket from a CAS 1 ticket validation response.
     * @param webResponse the web application validation response
     * @return a String representing the ticket
     * @throws IOException on IO error reading response
     * @throws RuntimeException on some parse failures
     */
    public static String serviceTicketFromResponse(final WebResponse webResponse) throws IOException {

        String serviceTicket;

        // now we need to extract the service ticket.

        // in a baseline CAS 2.x distribution return to the service is accomplished by
        // JavaScript redirect
        //
        // CAS 3 accomplishes this differently such that our client has already
        // followed the redirect to the service, so we'll find the service ticket
        // on the response URL.

        final String queryString = webResponse.getURL().getQuery();

        final int ticketIndex = queryString.indexOf("ticket=");

        if (ticketIndex == -1) {

            // the ticket wasn't in the response URL.
            // we're testing for CAS 2.x style JavaScript for redirection, as
            // recommended in appendix B of the CAS 2 protocol specification

            // parse the ticket out of the JavaScript

            final String response = webResponse.getText();

            final int declarationStartsAt = response.indexOf("window.location.href");
            // cut off the front of the response up to the beginning of the service URL
            final String responseAfterWindowLocHref = response.substring(declarationStartsAt
                    + "window.location.href12".length());

            // The URL might be single or double quoted
            final int endDoubleQuoteIndex = responseAfterWindowLocHref.indexOf("\"");
            final int endSingleQuoteIndex = responseAfterWindowLocHref.indexOf("\'");

            // we will set this variable to be the index of the first ' or " character
            int endQuoteIndex = 0;
            if (endDoubleQuoteIndex == -1 && endSingleQuoteIndex == -1) {
                throw new RuntimeException("Failed parsing a service ticket from the response:" + response);
            } else if (endDoubleQuoteIndex > -1 && (endDoubleQuoteIndex < endSingleQuoteIndex || endSingleQuoteIndex == -1)) {
                endQuoteIndex = endDoubleQuoteIndex;
            } else {
                endQuoteIndex = endSingleQuoteIndex;
            }

            final int ticketEqualsIndex = responseAfterWindowLocHref.indexOf("ticket=");

            serviceTicket = responseAfterWindowLocHref.substring(ticketEqualsIndex + "ticket=".length(), endQuoteIndex);


        } else {
            // service ticket was found on query String, parse it from there

            // TODO Is this type of redirection compatible?
            // Does it address all the issues that CAS2 JavaScript redirection
            // was intended to address?

            serviceTicket = queryString.substring(ticketIndex + "ticket=".length(), queryString.length());

        }

        return serviceTicket;
    }

}
