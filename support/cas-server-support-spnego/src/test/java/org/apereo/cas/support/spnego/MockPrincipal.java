package org.apereo.cas.support.spnego;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.security.Principal;
import lombok.Getter;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Slf4j
@Getter
@AllArgsConstructor
@ToString
public class MockPrincipal implements Principal {
    private final String name;
}
