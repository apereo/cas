package org.apereo.cas.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Service;

/**
 * This is {@link CasPromptProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service("casPromptProvider")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CasPromptProvider extends DefaultPromptProvider {
    @Override
    public String getPrompt() {
        return "cas>";
    }

    @Override
    public String getProviderName() {
        return "CAS";
    }
}
