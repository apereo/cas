package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OpenIdCredential implements Credential {
    private static final long serialVersionUID = -6535869729412406133L;

    private String ticketGrantingTicketId;
    private String username;

    @Override
    public String getId() {
        return this.username;
    }
}
