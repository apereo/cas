package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.util.transforms.BlockingPrincipalNameTransformer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlockingPrincipalNameTransformerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
public class BlockingPrincipalNameTransformerTests {
    @Test
    public void verifyBlocked() {
        val pt = new BlockingPrincipalNameTransformer("[abc]");
        assertThrows(PreventedException.class, () -> pt.transform("casuser"));
        assertEquals("helloworld", pt.transform("helloworld"));
    }

}
