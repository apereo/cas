package org.apereo.cas.adaptors.authy;

import com.authy.AuthyApiClient;
import com.authy.api.Token;
import com.authy.api.Tokens;
import com.authy.api.User;
import com.authy.api.Users;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.PostConstruct;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authy authentication handler for CAS.
 *
 * @author Misagh Moayyed
 * @since 5
 */
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

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
    public AuthyAuthenticationHandler(final String apiKey, final String apiUrl,
                                      final AuthyAccountRegistry registry) throws MalformedURLException {
        final URL url = new URL(apiUrl);
        final boolean testFlag = url.getProtocol().equals("http");

        this.authyClient = new AuthyApiClient(apiKey, apiUrl, testFlag);
        this.authyUsers = this.authyClient.getUsers();
        this.authyTokens = this.authyClient.getTokens();
        this.registry = registry;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (this.registry == null) {
            logger.warn("No Authy account registry is defined. All credentials are considered"
                            + "eligible for Authy authentication. Consider providing an account registry via [{}]",
                    AuthyAccountRegistry.class.getName());
        }
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final AuthyTokenCredential tokenCredential = (AuthyTokenCredential) credential;
        final RequestContext context = RequestContextHolder.getRequestContext();
        final Principal principal = WebUtils.getAuthentication(context).getPrincipal();
        
        if (!this.registry.contains(principal)) {
            final String email = (String) principal.getAttributes().get(this.mailAttribute);
            if (StringUtils.isBlank(email)) {
                throw new FailedLoginException("No email address found for " + principal.getId());
            }
            final String phone = (String) principal.getAttributes().get(this.phoneAttribute);
            if (StringUtils.isBlank(phone)) {
                throw new FailedLoginException("No phone number found for " + principal.getId());
            }

            final User user = authyUsers.createUser(email, phone);
            if (!user.isOk()) {
                throw new FailedLoginException(getAuthyErrorMessage(user.getError()));
            }
            final Integer authyId = user.getId();
            this.registry.add(authyId, principal);
        }

        final Integer authyId = this.registry.get(principal);

        final Map<String, String> options = new HashMap<>();
        options.put("force", this.forceVerification.toString());

        final Token verification = this.authyTokens.verify(authyId, tokenCredential.getToken(), options);

        if (!verification.isOk()) {
            throw new FailedLoginException(getAuthyErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, new ArrayStack());
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

    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
