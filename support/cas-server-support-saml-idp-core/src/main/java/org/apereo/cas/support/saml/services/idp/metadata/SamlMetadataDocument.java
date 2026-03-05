package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * This is {@link SamlMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "SamlMetadataDocument")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SamlMetadataDocument implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @Serial
    private static final long serialVersionUID = -721955605616455236L;
    
    @JsonProperty("id")
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private long id;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("value")
    @Lob
    @Column(name = "value", length = Integer.MAX_VALUE)
    private String value;

    @JsonProperty("signature")
    @Lob
    @Column(name = "signature", length = Integer.MAX_VALUE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String signature;

    /**
     * Gets base-64 decoded value if needed, or the value itself.
     *
     * @return the decoded value
     */
    @JsonIgnore
    public String getDecodedValue() {
        if (EncodingUtils.isBase64(value)) {
            return EncodingUtils.decodeBase64ToString(value);
        }
        return value;
    }

    /**
     * Assign id if necessary.
     *
     * @return the saml metadata document
     */
    @CanIgnoreReturnValue
    public SamlMetadataDocument assignIdIfNecessary() {
        if (id <= 0) {
            id = System.nanoTime();
        }
        return this;
    }

    /**
     * To json string.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
