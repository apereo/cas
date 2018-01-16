package org.apereo.cas.support.rest.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultUserAuthenticationResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultUserAuthenticationResourceEntityResponseFactory implements UserAuthenticationResourceEntityResponseFactory {


    private static final ObjectWriter MAPPER = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();

    @Override
    public ResponseEntity<String> build(final AuthenticationResult result, final HttpServletRequest request) throws Exception {
        return new ResponseEntity<>(MAPPER.writeValueAsString(result), HttpStatus.OK);
    }
}
