package org.apereo.cas.support.spnego;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Getter
@AllArgsConstructor
@ToString
public class MockPrincipal implements Principal {
    private final String name;
}
