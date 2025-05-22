package org.apereo.cas.notifications.call;

import org.apereo.cas.configuration.model.support.phone.PhoneProperties;
import org.apereo.cas.util.ResourceUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This is {@link PhoneCallBodyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@SuperBuilder
public class PhoneCallBodyBuilder implements Supplier<String> {
    @NonNull
    private final PhoneProperties properties;

    @Builder.Default
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    @Override
    public String get() {
        try {
            if (StringUtils.isBlank(properties.getText())) {
                LOGGER.warn("No phone call text is defined");
                return StringUtils.EMPTY;
            }
            val templateFile = ResourceUtils.getResourceFrom(properties.getText());
            try (val is = templateFile.getInputStream()) {
                val contents = IOUtils.toString(is, StandardCharsets.UTF_8);
                return formatBody(contents);
            }
        } catch (final Throwable e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return formatBody(properties.getText());
    }

    protected String formatBody(final String contents) {
        val sub = new StringSubstitutor(this.parameters, "${", "}");
        return sub.replace(contents);
    }
}
