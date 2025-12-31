package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

/**
 * This is {@link AmazonSimpleSystemsManagementPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("NullAway.Init")
@Slf4j
public class AmazonSimpleSystemsManagementPropertySource extends EnumerablePropertySource<SsmClient>
    implements MutablePropertySource<SsmClient> {

    private final Map<String, SsmProperty> properties = new LinkedHashMap<>();
    private final List<String> profiles;

    public AmazonSimpleSystemsManagementPropertySource(final String context,
                                                       final SsmClient ssmClient,
                                                       final List<String> profiles) {
        super(context, ssmClient);
        this.profiles = profiles;
        refresh();
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (properties.containsKey(name)) {
            val request = GetParameterRequest.builder()
                .name(properties.get(name).path())
                .withDecryption(Boolean.TRUE)
                .build();
            val parameter = getSource().getParameter(request).parameter();
            properties.put(name, new SsmProperty(parameter.name(), parameter.arn()));
            return parameter.value();
        }
        return null;
    }

    @Override
    public void refresh() {
        profiles.forEach(profile -> {
            var nextToken = (String) null;
            do {
                val prefix = String.format("/cas/%s", profile);
                val request = GetParametersByPathRequest.builder()
                    .path(prefix)
                    .withDecryption(Boolean.TRUE)
                    .nextToken(nextToken)
                    .build();
                val result = getSource().getParametersByPath(request);
                nextToken = result.nextToken();
                LOGGER.trace("Fetched [{}] parameters with next token as [{}]", result.parameters().size(), result.nextToken());
                result.parameters().forEach(parameter -> {
                    var propKey = Strings.CI.removeStart(parameter.name(), prefix);
                    propKey = Strings.CI.removeStart(propKey, "/");
                    val key = Strings.CI.removeEnd(propKey, "/");
                    properties.put(key, new SsmProperty(parameter.name(), parameter.arn()));
                });
            } while (nextToken != null);
        });
    }

    @Override
    public void removeProperty(final String name) {
        val path = properties.containsKey(name)
            ? properties.get(name).path()
            : StringUtils.join("/cas/", profiles.getLast(), "/", name);
        getSource().deleteParameter(builder -> builder.name(path).build());
        properties.remove(name);
    }

    @Override
    public void removeAll() {
        properties.keySet().forEach(this::removeProperty);
        properties.clear();
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val path = properties.containsKey(name)
            ? properties.get(name).path()
            : StringUtils.join("/cas/", profiles.getLast(), "/", name);
        getSource().putParameter(
            PutParameterRequest.builder()
                .name(path)
                .type(ParameterType.SECURE_STRING)
                .value(value.toString())
                .overwrite(Boolean.TRUE)
                .build()
        );
        properties.put(name, new SsmProperty(path, StringUtils.EMPTY));
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(String[]::new);
    }

    private record SsmProperty(String path, String arn) {
    }
}

