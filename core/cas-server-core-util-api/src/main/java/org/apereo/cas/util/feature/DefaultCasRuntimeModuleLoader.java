package org.apereo.cas.util.feature;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasRuntimeModuleLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DefaultCasRuntimeModuleLoader implements CasRuntimeModuleLoader {
    @Override
    @SneakyThrows
    public List<CasRuntimeModule> load() {
        val loader = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        return Arrays.stream(loader.getResources("classpath*:/git.properties"))
            .map(Unchecked.function(PropertiesLoaderUtils::loadProperties))
            .filter(props -> props.containsKey("project.name"))
            .map(props -> CasRuntimeModule.builder()
                .name(props.get("project.name").toString())
                .version(props.get("project.version").toString())
                .description(props.get("project.description").toString())
                .build())
            .sorted(Comparator.comparing(CasRuntimeModule::getName))
            .collect(Collectors.toList());
    }
}
