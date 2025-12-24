package org.apereo.cas.authentication;

import module java.base;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Generic CAS exception that sits at the top of the exception hierarchy. Provides
 * unified logic around retrieval and configuration of exception codes that may be
 * mapped inside an external resource bundle for internationalization of error messages.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Getter
@Accessors(chain = true)
@ToString(callSuper = true, of = "code")
@SuppressWarnings("OverrideThrowableToString")
public class RootCasException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2384466176716541689L;

    private final String code;

    private final List<Object> args = new ArrayList<>();

    protected RootCasException(final String code) {
        super(code);
        this.code = code;
    }

    protected RootCasException(final String code, @Nullable final String msg) {
        super(msg);
        this.code = code;
    }

    protected RootCasException(final String code, final String msg, final List<Object> args) {
        this(code, msg);
        this.args.addAll(args);
    }

    protected RootCasException(final String code, final Throwable throwable) {
        super(code, throwable);
        this.code = code;
    }

    protected RootCasException(final String code, final String msg, final Throwable throwable) {
        super(msg, throwable);
        this.code = code;
    }
    
    protected RootCasException(final String code, final Throwable throwable, final List<Object> args) {
        this(code, throwable);
        this.args.addAll(args);
    }

    /**
     * With code.
     *
     * @param code the code
     * @return the root cas exception
     */
    public static RootCasException withCode(final String code) {
        return new RootCasException(code, StringUtils.EMPTY);
    }

    /**
     * If there is a chained exception it recursively
     * calls {@code getCode()} on the cause of the chained exception rather than the returning
     * the code itself.
     *
     * @return Returns the code.
     */
    public String getCode() {
        val cause = this.getCause();
        if (cause instanceof final RootCasException rce) {
            return rce.getCode();
        }
        return this.code;
    }
}
