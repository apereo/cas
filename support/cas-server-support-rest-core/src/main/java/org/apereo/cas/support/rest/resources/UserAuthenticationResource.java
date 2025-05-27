package org.apereo.cas.support.rest.resources;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.UserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.security.auth.login.FailedLoginException;

/**
 * CAS RESTful resource for validating user credentials.
 * <ul>
 * <li>{@code POST /v1/users}</li>
 * </ul>
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("userAuthenticationResource")
@Slf4j
@Tag(name = "CAS REST")
@RequiredArgsConstructor
public class UserAuthenticationResource {
    private final RestAuthenticationService authenticationService;

    private final UserAuthenticationResourceEntityResponseFactory userAuthenticationResourceEntityResponseFactory;

    private final ApplicationContext applicationContext;

    /**
     * Authenticate requests.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request     raw HttpServletRequest used to call this method
     * @param response    the response
     * @return ResponseEntity representing RESTful response
     */
    @PostMapping(value = RestProtocolConstants.ENDPOINT_USERS,
        consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        },
        produces = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @Operation(summary = "Authenticate user credentials",
        parameters = @Parameter(name = "requestBody", required = true, description = "Username and password values"))
    public ResponseEntity<String> authenticateRequest(@RequestBody final MultiValueMap<String, String> requestBody,
                                                      final HttpServletRequest request,
                                                      final HttpServletResponse response) throws Throwable {
        try {
            val authenticationResult = authenticationService.authenticate(requestBody, request, response);
            val result = authenticationResult.orElseThrow(FailedLoginException::new);
            return this.userAuthenticationResourceEntityResponseFactory.build(result, request);
        } catch (final AuthenticationException e) {
            return RestResourceUtils.createResponseEntityForAuthnFailure(e, request, applicationContext);
        } catch (final BadRestRequestException e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return new ResponseEntity<>(StringEscapeUtils.escapeHtml4(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
