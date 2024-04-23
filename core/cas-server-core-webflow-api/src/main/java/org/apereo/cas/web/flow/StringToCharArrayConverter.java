package org.apereo.cas.web.flow;

import lombok.val;
import org.springframework.binding.convert.converters.Converter;

/**
 * This is {@link StringToCharArrayConverter}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class StringToCharArrayConverter implements Converter {
    /**
     * Single instance of this converter.
     */
    public static final Converter INSTANCE = new StringToCharArrayConverter();

    /**
     * The identifier of this converter.
     */
    public static final String ID = StringToCharArrayConverter.class.getSimpleName();

    @Override
    public Class<?> getSourceClass() {
        return String.class;
    }

    @Override
    public Class<?> getTargetClass() {
        return char.class;
    }

    @Override
    public Object convertSourceToTargetClass(final Object o, final Class<?> aClass) {
        val source = o.toString();
        val result = new char[source.length()];
        System.arraycopy(source.toCharArray(), 0, result, 0, source.length());
        return result;
    }
}
