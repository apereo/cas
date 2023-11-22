package org.apereo.cas.oidc.assurance.entity;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Verification}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Verification implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = 362826881836801556L;

    @JsonProperty("trust_framework")
    private String trustFramework;
    
    @JsonProperty("assurance_level")
    private String assuranceLevel;

    private String time;
    
    @JsonProperty("verification_process")
    private String verificationProcess;

    private List<Evidence> evidence = new ArrayList<>();

    @JsonProperty("assurance_process")
    private AssuranceProcess assuranceProcess;

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writer(new MinimalPrettyPrinter()).writeValueAsString(this));
    }
}
