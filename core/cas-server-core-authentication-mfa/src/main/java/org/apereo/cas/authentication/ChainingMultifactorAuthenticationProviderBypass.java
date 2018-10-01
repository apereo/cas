package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to Chain multiple MultifactorAuthenticationProviderBypass implementations.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class ChainingMultifactorAuthenticationProviderBypass implements MultifactorAuthenticationProviderBypass {

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
