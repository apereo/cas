package org.apereo.cas.git;

import org.apereo.cas.util.RegexUtils;

import lombok.val;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PathRegexPatternTreeFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class PathRegexPatternTreeFilterTests {

    @Test
    public void verifyOperation() {
        val filter = new PathRegexPatternTreeFilter(RegexUtils.createPattern(".+"));
        val walker = mock(TreeWalk.class);
        when(walker.isSubtree()).thenReturn(Boolean.TRUE);
        assertTrue(filter.include(walker));
        assertTrue(filter.shouldBeRecursive());
        assertNotNull(filter.clone());
    }
}
