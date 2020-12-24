package org.apereo.cas.initializr.info;

import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DependencyAliasesInfoContributor implements InfoContributor {
    private final InitializrMetadataProvider metadataProvider;

    @Override
    public void contribute(final Info.Builder builder) {
        Map<String, Object> details = new LinkedHashMap<>();
        metadataProvider.get().getDependencies().getAll()
            .stream()
            .filter(dependency -> !dependency.getAliases().isEmpty())
            .forEach(dependency -> details.put(dependency.getId(), dependency));
        if (!details.isEmpty()) {
            builder.withDetail("dependency-aliases", details);
        }
    }
}
