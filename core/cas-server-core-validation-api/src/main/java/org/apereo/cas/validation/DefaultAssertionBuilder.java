package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultAssertionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@SuperBuilder
public class DefaultAssertionBuilder {
    private final Authentication primaryAuthentication;

    @Builder.Default
    private final List<Authentication> authentications = new ArrayList<>(0);
    private final WebApplicationService service;
    private final boolean newLogin;

    private final RegisteredService registeredService;

    /**
     * Build assertion.
     *
     * @return the assertion
     */
    public Assertion assemble() {
        return new ImmutableAssertion(this.primaryAuthentication, this.authentications,
            this.newLogin, this.service, this.registeredService);
    }
}
