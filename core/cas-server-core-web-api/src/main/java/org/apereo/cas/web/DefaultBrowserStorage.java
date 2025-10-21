package org.apereo.cas.web;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.val;
import tools.jackson.databind.ObjectMapper;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private String context = "CasBrowserStorageContext";

    @Builder.Default
    private BrowserStorageTypes storageType = BrowserStorageTypes.SESSION;

    @Override
    @JsonIgnore
    public Map<String, Object> getPayloadJson() {
        return FunctionUtils.doUnchecked(() -> {
            val jsonPayload = MAPPER.readValue(this.payload, LinkedHashMap.class);
            if (jsonPayload.containsKey(this.context)) {
                val decoded = EncodingUtils.decodeBase64ToString(jsonPayload.get(this.context).toString());
                return MAPPER.readValue(decoded, LinkedHashMap.class);
            }
            return Map.of();
        });
    }

    @Override
    @JsonIgnore
    @CanIgnoreReturnValue
    public BrowserStorage setPayloadJson(final Object payload) {
        FunctionUtils.doAndHandle(_ -> setPayload(EncodingUtils.encodeBase64(MAPPER.writeValueAsString(payload))));
        return this;
    }

}
