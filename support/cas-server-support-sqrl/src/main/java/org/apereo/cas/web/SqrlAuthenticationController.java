package org.apereo.cas.web;

import org.jsqrl.server.JSqrlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link SqrlAuthenticationController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RestController("sqrlAuthenticationController")
public class SqrlAuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqrlAuthenticationController.class);

    private final JSqrlServer server;

    public SqrlAuthenticationController(final JSqrlServer server) {
        this.server = server;
    }

    /**
     * Sqrl response entity.
     *
     * @param httpRequest the http request
     * @return the response entity
     */
    @PostMapping(path = "/sqrl/authn")
    public ResponseEntity<String> sqrl(final HttpServletRequest httpRequest) {
        /*
        final SqrlAuthResponse sqrlAuthResponse = server.handleClientRequest(request, nut, httpRequest.getRemoteAddr());
        LOGGER.info("SQRL authentication response [{}] with nut [{}]", sqrlAuthResponse, nut);
        return new ResponseEntity(sqrlAuthResponse.toEncodedString(), HttpStatus.OK);
        */

        LOGGER.info("SQRL authentication response");
        return new ResponseEntity("", HttpStatus.OK);
    }

    /**
     * Sqrl authn response entity.
     *
     * @param httpRequest the http request
     * @return the response entity
     */
    @PostMapping(path = "/sqrl")
    public ResponseEntity<String> sqrlAuthn(final HttpServletRequest httpRequest) {
        /*
        final SqrlAuthResponse sqrlAuthResponse = server.handleClientRequest(request, nut, httpRequest.getRemoteAddr());
        LOGGER.info("SQRL authentication response [{}] with nut [{}]", sqrlAuthResponse, nut);
        return new ResponseEntity(sqrlAuthResponse.toEncodedString(), HttpStatus.OK);
        */

        LOGGER.info("SQRL authentication response 2");
        return new ResponseEntity("", HttpStatus.OK);
    }
}
