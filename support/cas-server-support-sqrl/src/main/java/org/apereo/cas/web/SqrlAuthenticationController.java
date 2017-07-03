package org.apereo.cas.web;

import org.jsqrl.model.SqrlAuthResponse;
import org.jsqrl.model.SqrlClientRequest;
import org.jsqrl.server.JSqrlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * @param request     the request
     * @param nut         the nut
     * @param httpRequest the http request
     * @return the response entity
     */
    @PostMapping(path = "/sqrl")
    public ResponseEntity sqrl(@ModelAttribute final SqrlClientRequest request,
                               @RequestParam("nut") final String nut,
                               final HttpServletRequest httpRequest) {
        LOGGER.error("TEST12");
        final SqrlAuthResponse sqrlAuthResponse = server.handleClientRequest(request, nut, httpRequest.getRemoteAddr());
        return new ResponseEntity(sqrlAuthResponse.toEncodedString(), HttpStatus.OK);
    }

    /**
     * Check authentication response entity.
     *
     * @param nut         the nut
     * @param httpRequest the http request
     * @return the response entity
     */
    @GetMapping(path = "/authcheck")
    public ResponseEntity checkAuthentication(@RequestParam("nut") final String nut,
                                              final HttpServletRequest httpRequest) {
        LOGGER.error("TEST1");
        if (server.checkAuthenticationStatus(nut, httpRequest.getRemoteAddr())) {
            return new ResponseEntity(HttpStatus.RESET_CONTENT);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
