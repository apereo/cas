package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
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
@Slf4j
@RequiredArgsConstructor
public class DisplayUserGraphicsBeforeAuthenticationAction extends AbstractAction {

    private final UserGraphicalAuthenticationRepository repository;

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        final var username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        final var graphics = repository.getGraphics(username);
        if (graphics == null || graphics.isEmpty()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        final var image = EncodingUtils.encodeBase64ToByteArray(graphics.read());
        WebUtils.putGraphicalUserAuthenticationUsername(requestContext, username);
        WebUtils.putGraphicalUserAuthenticationImage(requestContext, new String(image, StandardCharsets.UTF_8));
        return success();
    }
}
