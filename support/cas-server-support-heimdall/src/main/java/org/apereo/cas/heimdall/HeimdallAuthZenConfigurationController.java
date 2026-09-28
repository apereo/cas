package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authzen.AuthZenConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes the AuthZEN policy decision point metadata. The policy decision point is identified as
 * {@code <cas.server.prefix>/heimdall}; its well-known URL inserts {@code /.well-known/authzen-configuration}
 * after the host, which lies outside the CAS context and must be rewritten to {@link #ENDPOINT}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Authorization")
public class HeimdallAuthZenConfigurationController {
    /**
     * The well-known URI suffix of the AuthZEN metadata.
     */
    public static final String WELL_KNOWN_URI = "/.well-known/authzen-configuration";

    /**
     * The metadata endpoint, relative to the CAS context.
     */
    public static final String ENDPOINT = HeimdallAuthorizationController.BASE_URL + WELL_KNOWN_URI;

    private final CasConfigurationProperties casProperties;

    /**
     * AuthZEN policy decision point metadata.
     *
     * @return the metadata
     */
    @GetMapping(value = ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "AuthZEN policy decision point metadata")
    public AuthZenConfiguration configuration() {
        val policyDecisionPoint = StringUtils.removeEnd(casProperties.getServer().getPrefix(), "/")
            + HeimdallAuthorizationController.BASE_URL;
        return new AuthZenConfiguration(policyDecisionPoint, policyDecisionPoint + HeimdallAuthorizationController.AUTHZEN_PATH);
    }
}
