package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.soap.generated.ObjectFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link SoapAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class SoapAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final SoapAuthenticationClient soapAuthenticationClient;

    public SoapAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory, final Integer order,
                                     final SoapAuthenticationClient soapAuthenticationClient) {
        super(name, servicesManager, principalFactory, order);
        this.soapAuthenticationClient = soapAuthenticationClient;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        soapAuthenticationClient.setCredentials(credential);

        val request = new ObjectFactory().createGetSoapAuthenticationRequest();
        request.setUsername(credential.getUsername());
        val response = soapAuthenticationClient.sendRequest(request);

        if (response.getStatus() == HttpStatus.OK.value()) {
            val attributes = new LinkedHashMap<String, List<Object>>();
            response.getAttributes().forEach(item -> attributes.put(item.getKey().toString(), CollectionUtils.toCollection(item.getValue(), ArrayList.class)));
            val principal = principalFactory.createPrincipal(response.getUsername(), attributes);
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        }
        val httpStatus = HttpStatus.valueOf(response.getStatus());
        if (httpStatus.equals(HttpStatus.FORBIDDEN)) {
            throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
        }
        if (httpStatus.equals(HttpStatus.UNAUTHORIZED)) {
            throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
        }
        if (httpStatus.equals(HttpStatus.NOT_FOUND)) {
            throw new AccountNotFoundException("Could not locate account for " + credential.getUsername());
        }
        if (httpStatus.equals(HttpStatus.LOCKED)) {
            throw new AccountLockedException("Could not authenticate locked account for " + credential.getUsername());
        }
        if (httpStatus.equals(HttpStatus.PRECONDITION_FAILED)) {
            throw new AccountExpiredException("Could not authenticate expired account for " + credential.getUsername());
        }
        if (httpStatus.equals(HttpStatus.PRECONDITION_REQUIRED)) {
            throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
        }
        throw new FailedLoginException("SOAP endpoint returned an unknown status code "
            + httpStatus + " for " + credential.getUsername());

    }
}
