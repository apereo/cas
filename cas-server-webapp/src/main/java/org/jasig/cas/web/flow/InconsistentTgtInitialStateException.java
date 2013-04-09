package org.jasig.cas.web.flow;

/**
 * Exception that is thrown on a login attem with an inconsistent ticket granting ticket (TGT) initial state.
 *
 * @author David Ordas
 * @see CheckTgtStateInitialFlowSetupAction
 * @since 4.0.0
 */
public class InconsistentTgtInitialStateException extends RuntimeException {

    /**
     * The Unique ID for serialization.
     */
    private static final long serialVersionUID = 4747476760693524207L;

    /**
     * The code description.
     */
    private static final String CODE = "tgt.inconsistent.initial.state";

    public InconsistentTgtInitialStateException() {
        super(CODE);
    }

}
