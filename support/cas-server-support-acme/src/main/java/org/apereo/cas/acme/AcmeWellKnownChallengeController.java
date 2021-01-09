package org.apereo.cas.acme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AcmeWellKnownChallengeController}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RestController("acmeWellKnownChallengeController")
@Slf4j
@RequiredArgsConstructor
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
    public String handleRequest(@PathVariable(value = "token") final String token,
                              final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Handling ACME challenge...");
        return acmeChallengeRepository.get(token);
    }
}
