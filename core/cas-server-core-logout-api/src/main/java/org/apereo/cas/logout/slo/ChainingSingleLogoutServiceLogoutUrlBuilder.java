package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @SneakyThrows
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
    public boolean isServiceAuthorized(final WebApplicationService service, final Optional<HttpServletRequest> httpRequest) {
        return singleLogoutServiceLogoutUrlBuilders
            .stream()
            .anyMatch(builder -> builder.isServiceAuthorized(service, httpRequest));
    }
}
