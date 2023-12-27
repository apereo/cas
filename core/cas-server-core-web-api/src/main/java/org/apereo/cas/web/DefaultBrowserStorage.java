package org.apereo.cas.web;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.io.Serial;

/**
 * This is {@link DefaultBrowserStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@ToString
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@Accessors(chain = true)
public class DefaultBrowserStorage implements BrowserStorage {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = 775566570310426414L;

    private String payload;

    private String destinationUrl;

    @Builder.Default
    private String context = "casBrowserStorageContext";

    @Builder.Default
    private BrowserStorageTypes storageType = BrowserStorageTypes.SESSION;

    @Builder.Default
    private boolean removeOnRead = true;

    @CanIgnoreReturnValue
    @JsonIgnore
    @Override
    public BrowserStorage setPayloadJson(final Object data) {
        FunctionUtils.doUnchecked(__ -> setPayload(MAPPER.writeValueAsString(data)));
        return this;
    }

    @Override
    @JsonIgnore
    public <T> T getPayloadJson(final Class<T> clazz) {
        return FunctionUtils.doUnchecked(() -> MAPPER.readValue(this.payload, clazz));
    }
}
