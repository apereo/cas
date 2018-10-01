package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"order", "id"})
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148134156909L;

    private MultifactorAuthenticationProviderBypass bypassEvaluator;

    private String failureMode = "NOT_SET";

    private String id;

    private int order;

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public MultifactorAuthenticationProviderBypass getBypassEvaluator() {
        return this.bypassEvaluator;
    }

    @Override
    public RegisteredServiceMultifactorPolicy.FailureModes failureMode() {
        return RegisteredServiceMultifactorPolicy.FailureModes.valueOf(failureMode);
    }


    @Override
    public boolean matches(final String identifier) {
        return StringUtils.isNotBlank(getId()) ? getId().matches(identifier) : false;
    }
}
