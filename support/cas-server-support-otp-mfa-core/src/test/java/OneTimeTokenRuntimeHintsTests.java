import org.apereo.cas.nativex.OneTimeTokenRuntimeHints;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccountCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link OneTimeTokenRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
public class OneTimeTokenRuntimeHintsTests {
    @Test
    public void verifyHints() {
        val hints = new RuntimeHints();
        new OneTimeTokenRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(OneTimeTokenAccountCipherExecutor.class).test(hints));
    }
}
