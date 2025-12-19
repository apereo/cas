package org.apereo.cas.web.view.json;

import module java.base;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link CasJsonServiceResponseAuthenticationSuccess}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
public class CasJsonServiceResponseAuthenticationSuccess {

    private String user;

    private String proxyGrantingTicket;

    private List proxies;

    private Map attributes;
}
