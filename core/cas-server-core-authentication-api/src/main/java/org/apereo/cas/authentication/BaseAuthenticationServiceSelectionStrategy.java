package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.core.Ordered;
import java.io.Serial;
import java.util.LinkedHashMap;

/**
 * This is {@link BaseAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Setter
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {

    @Serial
    private static final long serialVersionUID = -7458940344679793681L;

    private final transient ServicesManager servicesManager;

    private final transient ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private int order = Ordered.HIGHEST_PRECEDENCE;

    protected Service createService(final String identifier, final Service original) {
        val result = webApplicationServiceFactory.createService(identifier);
        val attributes = new LinkedHashMap<>(original.getAttributes());
        attributes.put(Service.class.getName(), CollectionUtils.wrapList(original.getOriginalUrl()));
        result.setAttributes(attributes);
        return result;
    }
}
