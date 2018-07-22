package org.apereo.cas.configuration;

import lombok.val;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CommaSeparatedStringToThrowablesConverter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CommaSeparatedStringToThrowablesConverter implements Converter<String, List<Class<? extends Throwable>>> {
    @Override
    public List<Class<? extends Throwable>> convert(final String source) {
        try {
            val strings = StringUtils.commaDelimitedListToStringArray(source);
            val classes = new ArrayList<Class<? extends Throwable>>(strings.length);
            for (val className : strings) {
                classes.add((Class<? extends Throwable>) ClassUtils.forName(className.trim(), getClass().getClassLoader()));
            }
            return classes;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
