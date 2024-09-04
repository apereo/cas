package org.apereo.cas.util.serialization;

import com.fasterxml.jackson.core.PrettyPrinter;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

public abstract class AbstractJacksonBackedNonHjsonStringSerializer<T> extends AbstractJacksonBackedStringSerializer<T> {
    @Serial
    private static final long serialVersionUID = 2247573730512441790L;

    protected AbstractJacksonBackedNonHjsonStringSerializer(final PrettyPrinter prettyPrinter, final ConfigurableApplicationContext applicationContext) {
        super(prettyPrinter, applicationContext);
    }

    protected AbstractJacksonBackedNonHjsonStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected boolean mayBeHjson() {
        return false;
    }
}
