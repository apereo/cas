package org.apereo.cas.logout.slo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link SingleLogoutMessage}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Builder
@ToString(of = "payload")
public class SingleLogoutMessage<T> implements Serializable {
    private static final long serialVersionUID = -7763669015027355811L;

    private final String payload;

    private final transient T message;
}
