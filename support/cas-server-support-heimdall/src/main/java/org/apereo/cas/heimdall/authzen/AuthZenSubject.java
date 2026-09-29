package org.apereo.cas.heimdall.authzen;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;
import lombok.ToString;
import lombok.With;
import org.springframework.validation.annotation.Validated;

/**
 * This is {@link AuthZenSubject}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode
@SuperBuilder
@AllArgsConstructor
@With
public class AuthZenSubject implements Serializable {
    @Serial
    private static final long serialVersionUID = -6988573141680320570L;

    private String type;
    private String id;
    private Map<String, Object> properties;
}
