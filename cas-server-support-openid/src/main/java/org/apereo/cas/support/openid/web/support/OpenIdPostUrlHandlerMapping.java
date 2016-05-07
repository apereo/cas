package org.apereo.cas.support.openid.web.support;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

/**
 * OpenID url handling mappings.
 * @author Scott Battaglia
 * @since 3.1
 */
@RefreshScope
@Component("openIdPostUrlHandlerMapping")
public class OpenIdPostUrlHandlerMapping extends SimpleUrlHandlerMapping {

    @Autowired
    @Qualifier("openidDelegatingController")
    private Controller controller;

    @Override
    public void initApplicationContext() throws BeansException {
        setOrder(1);

        final Properties mappings = new Properties();
        mappings.put("/login", this.controller);
        setMappings(mappings);

        super.initApplicationContext();
    }

    @Override
    protected Object lookupHandler(final String urlPath, final HttpServletRequest request) throws Exception {
        if (HttpMethod.POST.name().equals(request.getMethod())
                && (OpenIdProtocolConstants.CHECK_AUTHENTICATION
                .equals(request.getParameter(OpenIdProtocolConstants.OPENID_MODE))
                    || OpenIdProtocolConstants.ASSOCIATE
                .equals(request.getParameter(OpenIdProtocolConstants.OPENID_MODE)))) {
            return super.lookupHandler(urlPath, request);
        }

        return null;
    }
}
