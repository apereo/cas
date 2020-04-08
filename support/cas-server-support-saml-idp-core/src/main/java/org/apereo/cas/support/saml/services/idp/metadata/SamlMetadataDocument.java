package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Lob;
import javax.persistence.Table;

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
@AllArgsConstructor
public class SamlMetadataDocument {

    @JsonProperty("id")
    @javax.persistence.Id
    @Id
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

    public SamlMetadataDocument() {
        setId(System.currentTimeMillis());
    }

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
