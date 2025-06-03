package org.apereo.cas.heimdall;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link BaseHeimdallEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SuperBuilder
@NoArgsConstructor
@Slf4j
public abstract class BaseHeimdallEntity implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -5470694907000490942L;

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }

    /**
     * Log.
     */
    public void log() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(toJson());
        }
    }
}
