package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class ChainingMultifactorAuthenticationBypassProvider implements MultifactorAuthenticationProviderBypass {

    private List<MultifactorAuthenticationProviderBypass> bypasses = new ArrayList<>();

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {

        for (final MultifactorAuthenticationProviderBypass bypass : bypasses) {
            if (!bypass.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add bypass provider.
     *
     * @param bypass - the bypass provider
     */
    public void addBypass(final MultifactorAuthenticationProviderBypass bypass) {
        this.bypasses.add(bypass);
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return bypasses.size();
    }
}
