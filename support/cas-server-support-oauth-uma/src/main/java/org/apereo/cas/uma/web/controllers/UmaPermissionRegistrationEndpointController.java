package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.support.oauth.OAuth20Constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link UmaPermissionRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Controller("umaPermissionRegistrationEndpointController")
@RequiredArgsConstructor
public class UmaPermissionRegistrationEndpointController {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    
    /**
     * Gets permission ticket.
     *
     * @param body the body
     * @return the permission ticket
     */
    @PostMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/" + OAuth20Constants.UMA_PERMISSION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPermissionTicket(@RequestBody final String body) {
        return new ResponseEntity("", HttpStatus.OK);
    }
}
