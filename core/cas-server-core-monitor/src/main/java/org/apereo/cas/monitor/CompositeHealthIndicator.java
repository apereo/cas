package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link CompositeHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class CompositeHealthIndicator extends AbstractHealthIndicator {
    private final List<? extends HealthIndicator> healthIndicators;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val aggregatedStatus = new ArrayList<Status>();
        healthIndicators.forEach(indicator -> {
            val health = indicator.health(true);
            if (health != null) {
                val name = (String) health.getDetails().getOrDefault("name", indicator.getClass().getSimpleName());
                val details = new LinkedHashMap<>(health.getDetails());
                details.computeIfAbsent("status", _ -> health.getStatus());
                builder.withDetail(name, details);
                aggregatedStatus.add(health.getStatus());
            }
        });
        val isUp = aggregatedStatus.stream().allMatch(s -> s.equals(Status.UP));
        builder.status(isUp ? Status.UP : Status.DOWN);
    }
}
