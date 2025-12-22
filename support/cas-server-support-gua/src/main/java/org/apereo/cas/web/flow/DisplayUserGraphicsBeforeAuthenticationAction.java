package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DisplayUserGraphicsBeforeAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DisplayUserGraphicsBeforeAuthenticationAction extends BaseCasWebflowAction {

    private final UserGraphicalAuthenticationRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            throw UnauthorizedServiceException.denied("Denied");
        }
        val graphics = repository.getGraphics(username);
        if (graphics == null || graphics.isEmpty()) {
            throw UnauthorizedServiceException.denied("Denied");
        }
        val image = EncodingUtils.encodeBase64ToByteArray(graphics.read());
        WebUtils.putGraphicalUserAuthenticationUsername(requestContext, username);
        WebUtils.putGraphicalUserAuthenticationImage(requestContext, new String(image, StandardCharsets.UTF_8));
        return success();
    }
}
