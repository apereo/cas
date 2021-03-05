package org.apereo.cas.git;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.util.regex.Pattern;

/**
 * This is {@link PathRegexPatternTreeFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class PathRegexPatternTreeFilter extends TreeFilter {
    private final Pattern pattern;

    @Override
    public boolean include(final TreeWalk walker) {
        if (walker.isSubtree()) {
            return true;
        }
        val pathString = walker.getPathString();
        return pattern.matcher(pathString).find();
    }

    @Override
    public boolean shouldBeRecursive() {
        return true;
    }

    @Override
    public TreeFilter clone() {
        return new PathRegexPatternTreeFilter(this.pattern);
    }
}
