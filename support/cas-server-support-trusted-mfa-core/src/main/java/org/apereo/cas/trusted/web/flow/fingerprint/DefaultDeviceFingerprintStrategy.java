package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public String determineFingerprintComponent(final Authentication authentication,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response) {
        return deviceFingerprintComponentManagers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .map(Unchecked.function(component -> component.extractComponent(authentication, request, response)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining(componentSeparator));
    }
}
