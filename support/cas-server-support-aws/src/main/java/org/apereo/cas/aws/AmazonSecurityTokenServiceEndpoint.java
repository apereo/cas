package org.apereo.cas.aws;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.aws.AmazonSecurityTokenServiceProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.awssdk.profiles.ProfileProperty;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link AmazonSecurityTokenServiceEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RestControllerEndpoint(id = "awsSts", enableByDefault = false)
@Slf4j
public class AmazonSecurityTokenServiceEndpoint extends BaseCasActuatorEndpoint {
    private final RestAuthenticationService restAuthenticationService;

    public AmazonSecurityTokenServiceEndpoint(final CasConfigurationProperties casProperties,
                                              final RestAuthenticationService restAuthenticationService) {
        super(casProperties);
        this.restAuthenticationService = restAuthenticationService;
    }

    private static ResponseEntity<String> createOutputResponse(final AmazonSecurityTokenServiceProperties amz,
                                                               final Credentials stsCredentials) {
        val properties = new LinkedHashMap<String, String>();
        properties.put(ProfileProperty.AWS_ACCESS_KEY_ID, stsCredentials.accessKeyId());
        properties.put(ProfileProperty.AWS_SECRET_ACCESS_KEY, stsCredentials.secretAccessKey());
        properties.put(ProfileProperty.AWS_SESSION_TOKEN, stsCredentials.sessionToken());
        properties.put(ProfileProperty.REGION, StringUtils.isBlank(amz.getRegion()) ? Region.AWS_GLOBAL.id() : Region.of(amz.getRegion()).id());

        val output = new StringBuilder("[default]\n");
        properties.forEach((key, value) -> output.append(String.format("%s=%s%n", key, value)));
        return ResponseEntity.ok(output.toString());
    }

    private static Optional<ResponseEntity<String>> authorizePrincipal(final AmazonSecurityTokenServiceProperties amz,
                                                                       final Principal principal) {
        if (StringUtils.isNotBlank(amz.getPrincipalAttributeName())) {
            if (!principal.getAttributes().containsKey(amz.getPrincipalAttributeName())) {
                LOGGER.error("Failed to locate authorization attribute for principal [{}]", principal);
                return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization failure"));
            }
            val attributeValues = principal.getAttributes().get(amz.getPrincipalAttributeName());
            if (StringUtils.isNotBlank(amz.getPrincipalAttributeValue())
                && attributeValues.stream().noneMatch(value -> RegexUtils.find(amz.getPrincipalAttributeValue(), value.toString()))) {
                LOGGER.error("Failed to locate authorization attribute value for principal [{}]", principal);
                return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization failure"));
            }
        }
        return Optional.empty();
    }

    /**
     * Fetch credentials.
     *
     * @param duration     the duration
     * @param tokenCode    the token code
     * @param profile      the profile
     * @param serialNumber the serial number
     * @param roleArn      the role arn
     * @param requestBody  the request body
     * @param request      the request
     * @return the map
     */
    @PostMapping
    @Operation(summary = "Fetch temporary credentials from Amazon Security Token Service", parameters = {
        @Parameter(name = "duration"),
        @Parameter(name = "tokenCode"),
        @Parameter(name = "profile"),
        @Parameter(name = "serialNumber"),
        @Parameter(name = "roleArn"),
        @Parameter(name = "requestBody"),
        @Parameter(name = "request")
    })
    public ResponseEntity<String> fetchCredentials(@RequestParam(required = false, defaultValue = "PT1H") final String duration,
                                                   @RequestParam(value = "token", required = false) final String tokenCode,
                                                   @RequestParam(required = false) final String profile,
                                                   @RequestParam(required = false) final String serialNumber,
                                                   @RequestParam(required = false) final String roleArn,
                                                   @RequestBody final MultiValueMap<String, String> requestBody,
                                                   final HttpServletRequest request) {

        var authenticationResult = (AuthenticationResult) null;
        try {
            authenticationResult = restAuthenticationService.authenticate(requestBody, request)
                .orElseThrow(AuthenticationException::new);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }

        val amz = casProperties.getAmazonSts();
        val principal = authenticationResult.getAuthentication().getPrincipal();
        LOGGER.debug("Authenticated principal: [{}]", principal);
        val authz = authorizePrincipal(amz, principal);
        if (authz.isPresent()) {
            return authz.get();
        }

        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), StringUtils.defaultString(profile, amz.getProfileName()));
        val builder = StsClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, credentials, amz);
        val client = builder.build();

        if (amz.isRbacEnabled()) {
            val attributeValues = principal.getAttributes().get(amz.getPrincipalAttributeName());
            LOGGER.debug("Found roles [{}]", attributeValues);

            if (attributeValues.size() > 1) {
                if (StringUtils.isBlank(roleArn)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Found multiple roles and none is specified. Current roles: " + attributeValues);
                }
                if (attributeValues.stream().noneMatch(value -> RegexUtils.find(roleArn, value.toString()))) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Specified role is not allowed. Current roles:" + attributeValues);
                }
            }
            val role = StringUtils.defaultString(roleArn, attributeValues.get(0).toString());
            LOGGER.debug("Using role [{}]", role);
            val roleRequest = AssumeRoleRequest.builder()
                .durationSeconds(Long.valueOf(Beans.newDuration(duration).toSeconds()).intValue())
                .roleArn(role)
                .roleSessionName(UUID.randomUUID().toString())
                .serialNumber(serialNumber)
                .tokenCode(tokenCode)
                .build();
            val sessionResult = client.assumeRole(roleRequest);
            val stsCredentials = sessionResult.credentials();
            return createOutputResponse(amz, stsCredentials);
        }

        val sessionTokenRequest = GetSessionTokenRequest.builder()
            .durationSeconds(Long.valueOf(Beans.newDuration(duration).toSeconds()).intValue())
            .serialNumber(serialNumber)
            .tokenCode(tokenCode)
            .build();
        val sessionResult = client.getSessionToken(sessionTokenRequest);
        val stsCredentials = sessionResult.credentials();
        return createOutputResponse(amz, stsCredentials);
    }
}
