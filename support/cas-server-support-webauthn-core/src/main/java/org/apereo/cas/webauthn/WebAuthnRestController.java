package org.apereo.cas.webauthn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yubico.internal.util.WebAuthnCodecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link WebAuthnRestController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RestController("webAuthnRestController")
@Slf4j
@RequestMapping(value = "/webauthn/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class WebAuthnRestController {

    private final WebAuthnServer server;
    private final ObjectMapper jsonMapper = WebAuthnCodecs.json();
    private final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
}
