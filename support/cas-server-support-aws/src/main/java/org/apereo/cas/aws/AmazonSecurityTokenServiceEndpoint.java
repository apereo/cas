package org.apereo.cas.aws;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

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
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;

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

    /**
     * Fetch credentials.
     *
     * @param duration     the duration
     * @param tokenCode    the token code
     * @param profile      the profile
     * @param serialNumber the serial number
     * @param requestBody  the request body
     * @param request      the request
     * @return the map
     */
    @PostMapping
    public ResponseEntity<String> fetchCredentials(@RequestParam(required = false, defaultValue = "PT1H") final String duration,
                                                   @RequestParam(value = "token", required = false) final String tokenCode,
                                                   @RequestParam(required = false) final String profile,
                                                   @RequestParam(required = false) final String serialNumber,
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
        if (StringUtils.isNotBlank(amz.getPrincipalAttributeName())) {
            if (!principal.getAttributes().containsKey(amz.getPrincipalAttributeName())) {
                LOGGER.error("Failed to locate authorization attribute for principal [{}]", principal);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization failure");
            }
            val attributeValues = principal.getAttributes().get(amz.getPrincipalAttributeName());
            if (attributeValues.stream().noneMatch(value -> RegexUtils.find(amz.getPrincipalAttributeValue(), value.toString()))) {
                LOGGER.error("Failed to locate authorization attribute value for principal [{}]", principal);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization failure");
            }
        }

        val credentials = ChainingAWSCredentialsProvider.getInstance(amz.getCredentialAccessKey(),
            amz.getCredentialSecretKey(), amz.getProfilePath(), StringUtils.defaultString(profile, amz.getProfileName()));

        val builder = StsClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, credentials, amz);
        val client = builder.build();
        val sessionTokenRequest = GetSessionTokenRequest.builder()
            .durationSeconds(Long.valueOf(Beans.newDuration(duration).toSeconds()).intValue())
            .serialNumber(serialNumber)
            .tokenCode(tokenCode)
            .build();
        val sessionResult = client.getSessionToken(sessionTokenRequest);
        val stsCredentials = sessionResult.credentials();

        val properties = new LinkedHashMap<String, String>();
        properties.put(ProfileProperty.AWS_ACCESS_KEY_ID, stsCredentials.accessKeyId());
        properties.put(ProfileProperty.AWS_SECRET_ACCESS_KEY, stsCredentials.secretAccessKey());
        properties.put(ProfileProperty.AWS_SESSION_TOKEN, stsCredentials.sessionToken());
        properties.put(ProfileProperty.REGION, StringUtils.isBlank(amz.getRegion()) ? Region.AWS_GLOBAL.id() : Region.of(amz.getRegion()).id());

        val output = new StringBuilder("[default]\n");
        properties.forEach((key, value) -> output.append(String.format("%s=%s%n", key, value)));
        return ResponseEntity.ok(output.toString());
    }
}
