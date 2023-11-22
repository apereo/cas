package org.apereo.cas.oidc.assurance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Attachment}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Attachment implements Serializable {
    @Serial
    private static final long serialVersionUID = 5992671812244121039L;

    private String desc;
    
    @JsonProperty("content_type")
    private String contentType;
}
