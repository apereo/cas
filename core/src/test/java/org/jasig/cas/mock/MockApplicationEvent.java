package org.jasig.cas.mock;

import org.springframework.context.ApplicationEvent;


public class MockApplicationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 3761968285092032567L;

    public MockApplicationEvent(Object arg0) {
        super(arg0);
    }

}
