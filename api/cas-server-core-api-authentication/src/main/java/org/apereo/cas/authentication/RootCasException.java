package org.apereo.cas.authentication;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic CAS exception that sits at the top of the exception hierarchy. Provides
 * unified logic around retrieval and configuration of exception codes that may be
 * mapped inside an external resource bundle for internationalization of error messages.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class RootCasException extends RuntimeException {

    private static final long serialVersionUID = -2384466176716541689L;

    private final String code;

    private final List<Object> args = new ArrayList<>(0);
    
    protected RootCasException(final String code, final String msg) {
        super(msg);
        this.code = code;
    }

    protected RootCasException(final String code, final String msg, final List<Object> args) {
        this(code, msg);
        this.args.addAll(args);
    }

    protected RootCasException(final String code, final Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    protected RootCasException(final String code, final Throwable throwable, final List<Object> args) {
        this(code, throwable);
        this.args.addAll(args);
    }


    /**
     * If there is a chained exception it recursively
     * calls {@code getCode()} on the cause of the chained exception rather than the returning
     * the code itself.
     * @return Returns the code.
     */
    public String getCode() {
        val cause = this.getCause();
        if (cause instanceof RootCasException) {
            return ((RootCasException) cause).getCode();
        }
        return this.code;
    }
}
