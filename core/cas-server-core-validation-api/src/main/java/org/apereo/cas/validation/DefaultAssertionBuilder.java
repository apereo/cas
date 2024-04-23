package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final Authentication originalAuthentication;

    @Builder.Default
    private final List<Authentication> authentications = new ArrayList<>(0);

    private final Service service;

    private final boolean newLogin;
    
    private final boolean stateless;

    private final RegisteredService registeredService;

    private final Map<String, Serializable> context;

    /**
     * Build assertion.
     *
     * @return the assertion
     */
    public Assertion assemble() {
        return new ImmutableAssertion(this.primaryAuthentication, this.originalAuthentication,
            this.authentications, this.newLogin, this.stateless, this.service, this.registeredService, this.context);
    }
}
