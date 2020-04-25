package org.apereo.cas;

import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link CasDisposableBean} that marks beans to be disposed
 * by {@link CasDestroyPrototypeBeansPostProcessorConfiguration}.
 *
 * @author leeyc0
 * @since 6.2.0
 */

public interface CasDisposableBean extends DisposableBean {
}
