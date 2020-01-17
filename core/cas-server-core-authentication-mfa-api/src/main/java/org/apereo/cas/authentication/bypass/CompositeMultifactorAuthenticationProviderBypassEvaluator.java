package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Multifactor Bypass Provider based on Authentication.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public class CompositeMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = 4482655921143779773L;

    private final List<MultifactorAuthenticationProviderBypassEvaluator> chain = new ArrayList<>();

    public CompositeMultifactorAuthenticationProviderBypassEvaluator(final String providerId) {
        super(providerId);
    }

    public void addBypassEvaluator(final MultifactorAuthenticationProviderBypassEvaluator evaluator) {
        chain.add(evaluator);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        return this.chain
            .stream()
            .allMatch(bypassEval -> bypassEval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request));
    }
}
