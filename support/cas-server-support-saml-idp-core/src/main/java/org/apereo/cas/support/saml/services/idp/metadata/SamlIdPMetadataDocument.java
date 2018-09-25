package org.apereo.cas.support.saml.services.idp.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigInteger;

/**
 * This is {@link SamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Entity
@Table(name = "SamlIdPMetadataDocument")
@Document
@Getter
@Setter
@AllArgsConstructor
public class SamlIdPMetadataDocument {

    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", columnDefinition = "BIGINT")
    private BigInteger id = BigInteger.valueOf(-1);

    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "metadata", length = 8_000)
    private String metadata;

    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "signingCertificate", length = 3_000)
    private String signingCertificate;

    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "signingKey", length = 3_000)
    private String signingKey;

    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "encryptionCertificate", length = 3_000)
    private String encryptionCertificate;

    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "encryptionKey", length = 3_000)
    private String encryptionKey;

    public SamlIdPMetadataDocument() {
        setId(BigInteger.valueOf(System.currentTimeMillis()));
    }
}
