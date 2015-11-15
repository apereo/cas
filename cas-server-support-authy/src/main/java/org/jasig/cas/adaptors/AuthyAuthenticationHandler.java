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

package net.unicon.cas.mfa.authentication.handler;

import com.authy.AuthyApiClient;
import com.authy.api.Token;
import com.authy.api.Tokens;
import com.authy.api.User;
import com.authy.api.Users;
import net.unicon.cas.mfa.web.flow.util.MultiFactorRequestContextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.adaptors.AuthyAccountRegistry;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.PostConstruct;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authy authentication handler for CAS.
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
        implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthyAuthenticationHandler.class);
    private static final long serialVersionUID = 4372937413518364597L;

    private final AuthyApiClient authyClient;
    private final Users authyUsers;
    private final Tokens authyTokens;

    private String mailAttribute = "mail";
    private String phoneAttribute = "phone";
    private Boolean forceVerification = Boolean.FALSE;

    private AuthyAccountRegistry registry;

    /**
     * Instantiates a new Authy authentication handler.
     *
     * @param apiKey the api key
     * @param apiUrl the api url
     * @throws MalformedURLException the malformed uRL exception
     */
    public AuthyAuthenticationHandler(@NotNull final String apiKey, @NotNull final String apiUrl,
                                      final AuthyAccountRegistry registry) throws MalformedURLException {
        final URL url = new URL(apiUrl);
        final boolean testFlag = url.getProtocol().equals("http");

        this.authyClient = new AuthyApiClient(apiKey, apiUrl, testFlag);
        this.authyUsers = this.authyClient.getUsers();
        this.authyTokens = this.authyClient.getTokens();
        this.registry = registry;
    }

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        if (this.registry == null) {
            logger.warn("No Authy account registry is defined. All credentials are considered"
                            + "eligible for Authy authentication. Consider providing an account registry via [{}]",
                    AuthyAccountRegistry.class.getName());
        }
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        final RequestContext context = RequestContextHolder.getRequestContext();

        if (!this.registry.contains(principal)) {
            final String email = (String) principal.getAttributes().get(this.mailAttribute);
            if (StringUtils.isBlank(email)) {
                throw new FailedLoginException("No email address found for " + principal.getId());
            }
            final String phone = (String) principal.getAttributes().get(this.phoneAttribute);
            if (StringUtils.isBlank(phone)) {
                throw new FailedLoginException("No phone number found for " + principal.getId();
            }

            final User user = authyUsers.createUser(email, phone);
            if (!user.isOk()) {
                throw new FailedLoginException(getAuthyErrorMessage(user.getError()));
            }
            final long authyId = user.getId();
            this.registry.add(authyId, principal);
        }

        final Long authyId = this.registry.get(principal);

        final Map<String, String> options = new HashMap<String, String>();
        options.put("force", this.forceVerification.toString());


        final Token verification = this.authyTokens.verify(authyId.intValue(),
                usernamePasswordCredentials.getUsername(), options);

        if (!verification.isOk()) {


            throw new FailedLoginException(getAuthyErrorMessage(verification.getError());
        }

        return ...;
    }

    public void setMailAttribute(final String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    public void setPhoneAttribute(final String phoneAttribute) {
        this.phoneAttribute = phoneAttribute;
    }

    public void setForceVerification(final Boolean forceVerification) {
        this.forceVerification = forceVerification;
    }

    /**
     * Gets authy error message.
     *
     * @param err the err
     * @return the authy error message
     */
    private static String getAuthyErrorMessage(final com.authy.api.Error err) {
        final StringBuilder builder = new StringBuilder();
        if (err != null) {
            builder.append("Authy Error");
            if (StringUtils.isNotBlank(err.getCountryCode())) {
                builder.append(": Country Code: ").append(err.getCountryCode());
            }
            if (StringUtils.isNotBlank(err.getMessage())) {
                builder.append(": Message: ").append(err.getMessage());
            }
        } else {
            builder.append("An unknown error has occurred. Check your API key and URL settings.");
        }
        return builder.toString();
    }
}
