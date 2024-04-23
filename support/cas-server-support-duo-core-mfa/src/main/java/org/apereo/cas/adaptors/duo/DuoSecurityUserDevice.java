package org.apereo.cas.adaptors.duo;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DuoSecurityUserDevice}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@SuperBuilder
@NoArgsConstructor(force = true)
@Jacksonized
public class DuoSecurityUserDevice implements Serializable {
    @Serial
    private static final long serialVersionUID = -6631171454545763954L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final String name;

    private final String type;

    private boolean activated;

    private String lastSeen;

    private String number;

    private String platform;

    private String id;

    private String model;

    @Builder.Default
    private List<String> capabilities = new ArrayList<>();

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }
}
