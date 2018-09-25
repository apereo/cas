package org.apereo.cas.support.saml.services.idp.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document
@Getter
@Setter
@AllArgsConstructor
public class SamlMetadataDocument {

    @JsonProperty("id")
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @JsonProperty("name")
    @Indexed
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
}
