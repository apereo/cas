package org.apereo.cas.web.flow;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

/**
 * This is {@link CasFlowHandlerMapping}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class CasFlowHandlerMapping extends FlowHandlerMapping implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        initApplicationContext();
    }
}
