package org.apereo.cas.aws;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.awssdk.profiles.ProfileProperty;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link AmazonSecurityTokenServiceEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RestControllerEndpoint(id = "awsSts", enableByDefault = false)
@Slf4j
public class AmazonSecurityTokenServiceEndpoint extends BaseCasActuatorEndpoint {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final RestHttpRequestCredentialFactory credentialFactory;

    private final RequestedAuthenticationContextValidator requestedContextValidator;

    public AmazonSecurityTokenServiceEndpoint(final CasConfigurationProperties casProperties,
                                              final AuthenticationSystemSupport authenticationSystemSupport,
                                              final RestHttpRequestCredentialFactory credentialFactory,
                                              final RequestedAuthenticationContextValidator requestedContextValidator) {
        super(casProperties);
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.credentialFactory = credentialFactory;
        this.requestedContextValidator = requestedContextValidator;
    }

    /**
     * Fetch credentials.
     *
     * @param duration    the duration
     * @param requestBody the request body
     * @param request     the request
     * @return the map
     */
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> fetchCredentials(@RequestParam(required = false, defaultValue = "PT1H") final String duration,
                                                   @RequestBody final MultiValueMap<String, String> requestBody,
                                                   final HttpServletRequest request) {
        val credential = this.credentialFactory.fromRequest(request, requestBody);
        if (credential == null || credential.isEmpty()) {
            return ResponseEntity.badRequest().body("No credentials are provided or extracted to authenticate the request");
        }
        val authenticationResult = getAuthenticationResult(credential);
        if (authenticationResult == null) {
            LOGGER.error("Unable to validate the authentication credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }

        val validationResult = requestedContextValidator.validateAuthenticationContext(request, null,
            authenticationResult.getAuthentication(), authenticationResult.getService());
        if (!validationResult.isSuccess()) {
            LOGGER.error("Unable to validate the authentication context");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication context validation failed");
        }

        val amz = casProperties.getAmazonSts();
        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), amz.getProfileName());

        val builder = StsClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, credentials, amz);
        val client = builder.build();
        val sessionTokenRequest = GetSessionTokenRequest.builder()
            .durationSeconds(Long.valueOf(Beans.newDuration(duration).toSeconds()).intValue())
            .build();
        val sessionResult = client.getSessionToken(sessionTokenRequest);
        val stsCredentials = sessionResult.credentials();

        val properties = new LinkedHashMap<String, String>();
        properties.put(ProfileProperty.AWS_ACCESS_KEY_ID, stsCredentials.accessKeyId());
        properties.put(ProfileProperty.AWS_SECRET_ACCESS_KEY, stsCredentials.secretAccessKey());
        properties.put(ProfileProperty.AWS_SESSION_TOKEN, stsCredentials.sessionToken());
        properties.put(ProfileProperty.REGION, StringUtils.isBlank(amz.getRegion()) ? Region.AWS_GLOBAL.id() : Region.of(amz.getRegion()).id());

        val result = new StringBuilder("[default]\n");
        properties.forEach((key, value) -> result.append(String.format("%s=%s%n", key, value)));
        return ResponseEntity.ok(result.toString());
    }

    private AuthenticationResult getAuthenticationResult(final List<Credential> credential) {
        try {
            return authenticationSystemSupport.finalizeAuthenticationTransaction(credential.toArray(Credential[]::new));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
