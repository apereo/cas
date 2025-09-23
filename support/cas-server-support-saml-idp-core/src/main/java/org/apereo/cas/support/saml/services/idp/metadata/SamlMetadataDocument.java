package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.util.EncodingUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;

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
public class SamlMetadataDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = -721955605616455236L;
    
    @JsonProperty("id")
    @jakarta.persistence.Id
    @Id
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private long id = -1;

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
}
