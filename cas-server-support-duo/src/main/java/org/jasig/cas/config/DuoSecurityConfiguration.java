package org.jasig.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This is {@link DuoSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("duoSecurityConfiguration")
public class DuoSecurityConfiguration {
    
    /**
     * Cas yubi key login view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casYubiKeyLoginView")
    public JstlView casYubiKeyLoginView() {
        return new JstlView("/WEB-INF/view/jsp/default/ui/casYubiKeyLoginView.jsp");
    }
    
}
