package org.apereo.cas.authentication.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link BasicIdentifiableCredential}, a simple credential implementation
 * that is only recognized by its id. The id generally represents an authentication token
 * encrypted in some fashion.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BasicIdentifiableCredential extends AbstractCredential {
    private static final long serialVersionUID = -700605020472810939L;

    private String id;
}
