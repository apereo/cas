package org.apereo.cas.services.support;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link RegisteredServiceScriptedAttributeFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceScriptedAttributeFilter implements RegisteredServiceAttributeFilter {
    private static final long serialVersionUID = 122972056984610198L;

    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern("groovy\\s*\\{(.+)\\}");
    private static final Pattern FILE_GROOVY_PATTERN = RegexUtils.createPattern("file:(.+\\.groovy)");

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceMappedRegexAttributeFilter.class);

    private int order;
    private String script;

    public RegisteredServiceScriptedAttributeFilter() {
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        final Matcher matcherInline = INLINE_GROOVY_PATTERN.matcher(script);
        final Matcher matcherFile = FILE_GROOVY_PATTERN.matcher(script);

        if (matcherInline.find()) {
            return filterInlinedGroovyAttributeValues(givenAttributes, matcherInline.group(1));
        }

        if (matcherFile.find()) {
            return filterFileBasedGroovyAttributeValues(givenAttributes, matcherFile.group(1));
        }

        return givenAttributes;
    }

    private static Map<String, Object> getGroovyAttributeValue(final String groovyScript,
                                                               final Map<String, Object> resolvedAttributes) {
        try {
            final Binding binding = new Binding();
            final GroovyShell shell = new GroovyShell(binding);
            binding.setVariable("attributes", resolvedAttributes);
            binding.setVariable("logger", LOGGER);
            final Map<String, Object> res = (Map<String, Object>) shell.evaluate(groovyScript);
            return res;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>();
    }

    private static Map<String, Object> filterInlinedGroovyAttributeValues(final Map<String, Object> resolvedAttributes,
                                                                          final String script) {
        LOGGER.debug("Found inline groovy script to execute [{}]", script);
        final Map<String, Object> attributesToRelease = getGroovyAttributeValue(script, resolvedAttributes);
        return attributesToRelease;
    }

    private static Map<String, Object> filterFileBasedGroovyAttributeValues(final Map<String, Object> resolvedAttributes,
                                                                            final String scriptFile) {
        try {
            LOGGER.debug("Found groovy script file to execute [{}]", scriptFile);
            final String script = FileUtils.readFileToString(new File(scriptFile), StandardCharsets.UTF_8);
            final Map<String, Object> attributesToRelease = getGroovyAttributeValue(script, resolvedAttributes);
            return attributesToRelease;
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("order", order)
                .append("script", script)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RegisteredServiceScriptedAttributeFilter rhs = (RegisteredServiceScriptedAttributeFilter) obj;
        return new EqualsBuilder()
                .append(this.order, rhs.order)
                .append(this.script, rhs.script)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(order)
                .append(script)
                .toHashCode();
    }
}
