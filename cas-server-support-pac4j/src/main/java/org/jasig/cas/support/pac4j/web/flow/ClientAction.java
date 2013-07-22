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
package org.jasig.cas.support.pac4j.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.jasig.cas.web.support.WebUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.Protocol;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class represents an action to put at the beginning of the webflow.<br />
 * Before any authentication, redirection urls are computed for the different clients defined as well as the theme,
 * locale, method and service are saved into the web session.<br />
 * After authentication, appropriate information are expected on this callback url to finish the authentication
 * process with the provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ClientAction extends AbstractAction {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ClientAction.class);

    /**
     * Constant for the service parameter.
     */
    public static final String SERVICE = "service";
    /**
     * Constant for the theme parameter.
     */
    public static final String THEME = "theme";
    /**
     * Constant for the locale parameter.
     */
    public static final String LOCALE = "locale";
    /**
     * Constant for the method parameter.
     */
    public static final String METHOD = "method";

    /**
     * The clients used for authentication.
     */
    @NotNull
    private final Clients clients;

    /**
     * The service for CAS authentication.
     */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /**
     * Build the action.
     *
     * @param theCentralAuthenticationService The service for CAS authentication
     * @param theClients The clients for authentication
     */
    public ClientAction(final CentralAuthenticationService theCentralAuthenticationService,
            final Clients theClients) {
        this.centralAuthenticationService = theCentralAuthenticationService;
        this.clients = theClients;
        ProfileHelper.setKeepRawData(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // get client
        final String clientName = request.getParameter(this.clients.getClientNameParameter());
        logger.debug("clientName : {}", clientName);

        // it's an authentication
        if (StringUtils.isNotBlank(clientName)) {
            // get client
            final BaseClient<Credentials, CommonProfile> client =
                    (BaseClient<Credentials, CommonProfile>) this.clients
                    .findClient(clientName);
            logger.debug("client : {}", client);

            // HTTP protocol not allowed
            if (client.getProtocol() == Protocol.HTTP) {
                throw new TechnicalException("HTTP protocol client not supported : " + client);
            }

            // get credentials
            final Credentials credentials;
            try {
                credentials = client.getCredentials(webContext);
                logger.debug("credentials : {}", credentials);
            } catch (final RequiresHttpAction e) {
                logger.info("requires http action : {}", e);
                response.flushBuffer();
                ExternalContext externalContext = ExternalContextHolder.getExternalContext();
                externalContext.recordResponseComplete();
                return new Event(this, "stop");
            }

            // retrieve parameters from web session
            final Service service = (Service) session.getAttribute(SERVICE);
            context.getFlowScope().put(SERVICE, service);
            restoreRequestAttribute(request, session, THEME);
            restoreRequestAttribute(request, session, LOCALE);
            restoreRequestAttribute(request, session, METHOD);

            // credentials not null -> try to authenticate
            if (credentials != null) {
                WebUtils.putTicketGrantingTicketInRequestScope(context,
                        this.centralAuthenticationService.createTicketGrantingTicket(new ClientCredential(credentials)));
                return success();
            }
        }

        // no or aborted authentication : go to login page
        prepareForLoginPage(context);
        return error();
    }

    /**
     * Prepare the data for the login page.
     *
     * @param context The current webflow context
     */
    protected void prepareForLoginPage(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        final HttpSession session = request.getSession();

        // web context
        final WebContext webContext = new J2EContext(request, response);

        // save parameters in web session
        final Service service = (Service) context.getFlowScope().get(SERVICE);
        if (service != null) {
            session.setAttribute(SERVICE, service);
        }
        saveRequestParameter(request, session, THEME);
        saveRequestParameter(request, session, LOCALE);
        saveRequestParameter(request, session, METHOD);

        // for all clients, generate redirection urls
        for (final Client client : this.clients.findAllClients()) {
            final String key = client.getName() + "Url";
            final String redirectionUrl = client.getRedirectionUrl(webContext);
            logger.debug("{} -> {}", key, redirectionUrl);
            context.getFlowScope().put(key, redirectionUrl);
        }
    }

    /**
     * Restore an attribute in web session as an attribute in request.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name The name of the parameter
     */
    private void restoreRequestAttribute(final HttpServletRequest request, final HttpSession session,
            final String name) {
        final String value = (String) session.getAttribute(name);
        request.setAttribute(name, value);
    }

    /**
     * Save a request parameter in the web session.
     *
     * @param request The HTTP request
     * @param session The HTTP session
     * @param name The name of the parameter
     */
    private void saveRequestParameter(final HttpServletRequest request, final HttpSession session,
            final String name) {
        final String value = request.getParameter(name);
        if (value != null) {
            session.setAttribute(name, value);
        }
    }
}
