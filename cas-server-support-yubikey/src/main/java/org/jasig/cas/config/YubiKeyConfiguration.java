package org.jasig.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yubikeyConfiguration")
public class YubiKeyConfiguration {

    /**
     * Cas duo login view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casDuoLoginView")
    public JstlView casDuoLoginView() {
        return new JstlView("/WEB-INF/view/jsp/default/ui/casDuoLoginView.jsp");
    }
}
