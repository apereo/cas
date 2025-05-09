package org.apereo.cas.acme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AcmeWellKnownChallengeController}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@RestController("acmeWellKnownChallengeController")
@Slf4j
@SuppressWarnings("removal")
@RequiredArgsConstructor
@Deprecated(since = "7.3.0", forRemoval = true)
public class AcmeWellKnownChallengeController {
    private final AcmeChallengeRepository acmeChallengeRepository;

    /**
     * Handle request.
     *
     * @param token    the token
     * @param request  the request
     * @param response the response
     * @return the string
     */
    @GetMapping(value = "/.well-known/acme-challenge/{token}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String handleRequest(
        @PathVariable("token")
        final String token,
        final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Handling ACME challenge...");
        return acmeChallengeRepository.get(token);
    }
}
