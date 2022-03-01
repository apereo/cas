package org.apereo.cas.util.spring;

import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ListFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DisposableListFactoryBean}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DisposableListFactoryBean extends ListFactoryBean {
    public DisposableListFactoryBean() {
        setSourceList(new ArrayList<>());
    }

    @Override
    protected void destroyInstance(final List list) {
        if (list != null) {
            list.forEach(Unchecked.consumer(postProcessor ->
                ((DisposableBean) postProcessor).destroy()
            ));
        }
    }
}
