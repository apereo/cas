package org.apereo.cas.web;

import org.jsqrl.model.SqrlAuthResponse;
import org.jsqrl.model.SqrlClientRequest;
import org.jsqrl.server.JSqrlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping(path = "/sqrl/authn")
    public ResponseEntity<String> sqrl(@ModelAttribute final SqrlClientRequest request,
                                       @RequestParam("nut") final String nut,
                                       final HttpServletRequest httpRequest) {
        final String remoteAddr = httpRequest.getRemoteAddr();
        LOGGER.info("SQRL authentication response command [{}] w/ client: [{}] and Parameters [{}]. "
                        + "Decoded client data [{}] w/ server [{}]'s decoded data [{}]. "
                        + "Request version [{}] with ids [{}] and urs [{}]. Remote address is [{}]",
                request.getCommand(), request.getClient(), request.getClientParameters(),
                request.getDecodedClientData(), request.getServer(), request.getDecodedServerData(),
                request.getRequestVersion(), request.getIds(), request.getUrs(), remoteAddr);

        try {
            LOGGER.info("Handling SQRL authentication client request for  nut [{}]", nut);
            final SqrlAuthResponse sqrlAuthResponse = server.handleClientRequest(request, nut, remoteAddr);
            LOGGER.info("SQRL authentication response [{}] for nut [{}]", sqrlAuthResponse, nut);
            final String s = sqrlAuthResponse.toEncodedString();
            LOGGER.info("Returning encoded response [{}] with status [{}]", s, HttpStatus.OK);
            return new ResponseEntity(s, HttpStatus.OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Check authentication response entity.
     *
     * @param nut         the nut
     * @param httpRequest the http request
     * @return the response entity
     */
    @GetMapping(path = "/sqrl/authcheck")
    public ResponseEntity checkAuthentication(@RequestParam("nut") final String nut,
                                              final HttpServletRequest httpRequest) {
        final String remoteAddr = httpRequest.getRemoteAddr();
        LOGGER.debug("Checking for SQRL authentication success against nut [{}] for client [{}]", nut, remoteAddr);

        if (server.checkAuthenticationStatus(nut, remoteAddr)) {
            LOGGER.info("SQRL authentication request [{}] is authenticated. Returning status [{}]", remoteAddr, HttpStatus.RESET_CONTENT);
            return new ResponseEntity(HttpStatus.RESET_CONTENT);
        }
        LOGGER.debug("SQRL request is not authenticated yet");
        return new ResponseEntity(HttpStatus.OK);
    }
}
