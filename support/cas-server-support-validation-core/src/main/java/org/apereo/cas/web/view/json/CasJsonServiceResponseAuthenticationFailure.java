package org.apereo.cas.web.view.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link CasJsonServiceResponseAuthenticationFailure}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
public class CasJsonServiceResponseAuthenticationFailure {
    private String code;
    private String description;
}
