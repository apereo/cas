package org.apereo.cas.web.flow;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.EncodingUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link DisplayUserGraphicsBeforeAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DisplayUserGraphicsBeforeAuthenticationAction extends AbstractAction {
    private final GraphicalUserAuthenticationProperties guaProperties;
    private final ServicesManager servicesManager;
    private final UserGraphicalAuthenticationRepository repository;

    public DisplayUserGraphicsBeforeAuthenticationAction(final ServicesManager servicesManager,
                                                         final GraphicalUserAuthenticationProperties guaProperties, 
                                                         final UserGraphicalAuthenticationRepository repository) {
        this.guaProperties = guaProperties;
        this.servicesManager = servicesManager;
        this.repository = repository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        final ByteSource graphics = repository.getGraphics(username);
        if (graphics == null || graphics.isEmpty()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        final byte[] image = EncodingUtils.encodeBase64ToByteArray(graphics.read());
        requestContext.getFlowScope().put("guaUsername", username);
        requestContext.getFlowScope().put("guaUserImage", new String(image, StandardCharsets.UTF_8));
        return success();
    }
}
