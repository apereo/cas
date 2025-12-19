package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link DuoSecurityAuthenticationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuperBuilder
@Getter
@ToString
public class DuoSecurityAuthenticationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 7395705948526013102L;

    private final boolean success;

    private final String username;

    @Builder.Default
    private final Map<String, List<Object>> attributes = new LinkedHashMap<>();
}
