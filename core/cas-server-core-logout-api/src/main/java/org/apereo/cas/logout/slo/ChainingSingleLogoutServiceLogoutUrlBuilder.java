package org.apereo.cas.logout.slo;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ChainingSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ChainingSingleLogoutServiceLogoutUrlBuilder implements SingleLogoutServiceLogoutUrlBuilder {
    private final List<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilders;

    @Override
    public Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                          final WebApplicationService singleLogoutService,
                                                          final Optional<HttpServletRequest> httpRequest) {
        return singleLogoutServiceLogoutUrlBuilders
            .stream()
            .sorted(Comparator.comparing(SingleLogoutServiceLogoutUrlBuilder::getOrder))
            .filter(builder -> builder.supports(registeredService, singleLogoutService, httpRequest))
            .map(builder -> builder.determineLogoutUrl(registeredService, singleLogoutService, httpRequest))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return singleLogoutServiceLogoutUrlBuilders
            .stream()
            .anyMatch(builder -> builder.supports(registeredService, singleLogoutService, httpRequest));
    }

    @Override
    public boolean isServiceAuthorized(final WebApplicationService service,
                                       final Optional<HttpServletRequest> httpRequest,
                                       final Optional<HttpServletResponse> httpResponse) {
        return singleLogoutServiceLogoutUrlBuilders
            .stream()
            .anyMatch(builder -> builder.isServiceAuthorized(service, httpRequest, httpResponse));
    }
}
