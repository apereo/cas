package org.apereo.cas.authentication;

import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.RegisteredService;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"order", "id"})
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148134156909L;

    private MultifactorAuthenticationProviderBypassEvaluator bypassEvaluator;

    private MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    private MultifactorAuthenticationProviderFailureModes failureMode = MultifactorAuthenticationProviderFailureModes.UNDEFINED;

    private String id;

    private int order;

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return true;
    }

    @Override
    public boolean matches(final String identifier) {
        return StringUtils.isNotBlank(getId()) && StringUtils.isNotBlank(identifier) && getId().matches(identifier);
    }
}
