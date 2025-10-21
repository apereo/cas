package org.apereo.cas.web.flow.actions.storage;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;
import java.util.Locale;

/**
 * This is {@link BaseBrowserStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
@Setter
public abstract class BaseBrowserStorageAction extends BaseCasWebflowAction {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    protected final CasCookieBuilder ticketGrantingCookieBuilder;

    protected String browserStorageContextKey = "CasBrowserStorageContext";

    protected BrowserStorage.BrowserStorageTypes determineStorageType(final RequestContext requestContext) {
        val requestScope = requestContext.getRequestScope();
        if (requestScope.contains(BrowserStorage.BrowserStorageTypes.class.getSimpleName())) {
            val requiredType = requestScope.getRequiredString(BrowserStorage.BrowserStorageTypes.class.getSimpleName());
            return BrowserStorage.BrowserStorageTypes.valueOf(requiredType.toUpperCase(Locale.ENGLISH));
        }
        return BrowserStorage.BrowserStorageTypes.LOCAL;
    }
}
