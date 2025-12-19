package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Evidence}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Evidence implements Serializable {
    @Serial
    private static final long serialVersionUID = 789274718595178967L;

    private String type;

    private String method;

    private String time;

    @Deprecated(since = "7.0.0")
    private Document document;

    @JsonProperty("document_details")
    private DocumentDetails documentDetails;

    @JsonProperty("validation_method")
    private ValidationMethod validationMethod;

    @JsonProperty("verification_method")
    private VerificationMethod verificationMethod;

    private Verifier verifier;

    private AssuranceRecord record;

    private List<Attachment> attachments = new ArrayList<>();
}


