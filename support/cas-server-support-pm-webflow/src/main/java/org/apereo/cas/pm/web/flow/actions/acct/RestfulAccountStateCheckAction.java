package org.apereo.cas.pm.web.flow.actions.acct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RestfulAccountStateCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestfulAccountStateCheckAction extends BaseAccountStateCheckAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulAccountStateCheckAction.class);
    
    private final RestTemplate restTemplate;
    private final String endpoint;

    public RestfulAccountStateCheckAction(final RestTemplate restTemplate, final String endpoint) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return super.doExecute(requestContext);
    }
}
