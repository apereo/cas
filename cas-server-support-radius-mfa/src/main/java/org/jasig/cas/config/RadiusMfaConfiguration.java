package org.jasig.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This is {@link RadiusMfaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
public class RadiusMfaConfiguration {

    /**
     * Cas radius login view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "casRadiusLoginView")
    public JstlView casRadiusLoginView() {
        return new JstlView("/WEB-INF/view/jsp/default/ui/casRadiusLoginView.jsp");
    }

}
