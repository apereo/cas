package org.apereo.cas.support.saml.web.idp.profile.builders;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AuthenticatedAssertionContext}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
@ToString(of = "name")
public class AuthenticatedAssertionContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 8177552946469646942L;

    @Builder.Default
    private final ZonedDateTime validFromDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    private final ZonedDateTime validUntilDate = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);

    @Builder.Default
    private final ZonedDateTime authenticationDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private final String name;
}
