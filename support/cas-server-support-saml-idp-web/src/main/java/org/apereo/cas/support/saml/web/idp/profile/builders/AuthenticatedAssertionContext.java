package org.apereo.cas.support.saml.web.idp.profile.builders;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
