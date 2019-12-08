package org.apereo.cas.uma.ticket.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * This is {@link InvalidResourceSetException}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
public class InvalidResourceSetException extends RuntimeException {
    private static final long serialVersionUID = 7631083183310661586L;

    private final int code;
    private final String message;

    public HttpStatus getStatus() {
        return HttpStatus.valueOf(this.code);
    }
}
