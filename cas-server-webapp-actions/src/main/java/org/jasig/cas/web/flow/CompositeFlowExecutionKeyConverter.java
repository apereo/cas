package org.jasig.cas.web.flow;

import org.springframework.binding.convert.converters.Converter;
import org.springframework.webflow.execution.repository.support.CompositeFlowExecutionKey;


/**
 * Special converter for the {@link CompositeFlowExecutionKey} to return as a String.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@SuppressWarnings("rawtypes")
public final class CompositeFlowExecutionKeyConverter implements Converter {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSourceClass() {
        return CompositeFlowExecutionKey.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getTargetClass() {
        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertSourceToTargetClass(final Object source, final Class targetClass) throws Exception {
        return source.toString();
    }
}
