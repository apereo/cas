package org.apereo.cas.trusted.web.flow.fingerprint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default {@link DeviceFingerprintStrategy} implementation that uses {@link DeviceFingerprintComponentManager} to generate
 * a fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultDeviceFingerprintStrategy implements DeviceFingerprintStrategy {
    private final List<DeviceFingerprintComponentManager> deviceFingerprintComponentManagers;

    private final String componentSeparator;

    @Override
    public String determineFingerprintComponent(final String principal, final RequestContext context) {
        return deviceFingerprintComponentManagers
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .map(component -> component.extractComponent(principal, context))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining(componentSeparator));
    }
}
