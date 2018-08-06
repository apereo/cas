package org.apereo.cas.support.spnego;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Getter
@RequiredArgsConstructor
@ToString
public class MockPrincipal implements Principal {
    private final String name;
}
