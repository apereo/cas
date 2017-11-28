package org.apereo.cas.support.saml.services.idp.metadata;

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
public class SamlMetadataDocument {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Indexed
    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String name;

    @Lob
    @Column(name = "value", length = Integer.MAX_VALUE)
    private String value;

    @Lob
    @Column(name = "signature", length = Integer.MAX_VALUE)
    private String signature;

    public SamlMetadataDocument() {
        setId(System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }
}
