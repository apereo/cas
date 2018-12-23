package org.apereo.cas.trusted.web.flow.fingerprint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.OrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default {@link DeviceFingerprintStrategy} implementation that uses {@link DeviceFingerprintComponentExtractor} to generate
 * a fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DefaultDeviceFingerprintStrategy implements DeviceFingerprintStrategy {
    private final @NonNull List<DeviceFingerprintComponentExtractor> componentExtractors;
    private final @NonNull String componentSeparator;

    @Override
    public String determineFingerprint(final String principal, final RequestContext context, final boolean isNew) {
        return componentExtractors
            .stream()
            .sorted(OrderComparator.INSTANCE)
            .map(component -> component.extractComponent(principal, context, isNew))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining(componentSeparator));
    }
}
