package org.apereo.cas.git;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ChainingCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link GitRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GitRepository implements DisposableBean {
    private final Git gitInstance;

    @Getter
    private final List<CredentialsProvider> credentialsProvider;

    private final TransportConfigCallback transportConfigCallback;

    private final long timeoutInSeconds;

    private final boolean signCommits;

    /**
     * Gets repository directory.
     *
     * @return the repository directory
     */
    public File getRepositoryDirectory() {
        return this.gitInstance.getRepository().getDirectory().getParentFile();
    }

    /**
     * Gets objects in repository.
     *
     * @return the objects in repository
     */
    @SneakyThrows
    public Collection<GitObject> getObjectsInRepository() {
        return getObjectsInRepository(TreeFilter.ALL);
    }

    /**
     * Gets objects in repository.
     *
     * @param filter the filter
     * @return the objects in repository
     */
    @SneakyThrows
    public Collection<GitObject> getObjectsInRepository(final TreeFilter filter) {
        val repository = this.gitInstance.getRepository();
        val head = repository.resolve(Constants.HEAD);

        try (val walk = new RevWalk(repository)) {
            val commit = walk.parseCommit(head);
            val tree = commit.getTree();
            try (val treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(filter);
                val list = new ArrayList<GitObject>();
                while (treeWalk.next()) {
                    val object = readObject(treeWalk);
                    list.add(object);
                }
                return list;
            }
        }
    }

    /**
     * Read object.
     *
     * @param treeWalk the tree walk
     * @return the git object
     */
    @SneakyThrows
    public GitObject readObject(final TreeWalk treeWalk) {
        val objectId = treeWalk.getObjectId(0);
        val repository = this.gitInstance.getRepository();
        val loader = repository.open(objectId);
        val out = new ByteArrayOutputStream();
        loader.copyTo(out);
        return GitObject.builder()
            .content(out.toString(StandardCharsets.UTF_8))
            .treeWalk(treeWalk)
            .objectId(objectId)
            .build();
    }

    /**
     * Commit all.
     *
     * @param message the message
     */
    @SneakyThrows
    public void commitAll(final String message) {
        this.gitInstance.add().addFilepattern(".").call();
        this.gitInstance.commit()
            .setMessage(message)
            .setAll(true)
            .setSign(this.signCommits)
            .setAuthor("CAS", "cas@apereo.org")
            .call();
    }

    /**
     * Push.
     */
    @SneakyThrows
    public void push() {
        if (!this.credentialsProvider.isEmpty()) {
            val providers = this.credentialsProvider.toArray(CredentialsProvider[]::new);
            this.gitInstance.push()
                .setTimeout((int) timeoutInSeconds)
                .setTransportConfigCallback(this.transportConfigCallback)
                .setPushAll()
                .setCredentialsProvider(new ChainingCredentialsProvider(providers))
                .call();
        } else {
            LOGGER.debug("No credentials are provided. Changes will not be pushed to repository");
        }
    }

    /**
     * Pull repository changes.
     *
     * @return true/false
     */
    @SneakyThrows
    public boolean pull() {
        val providers = this.credentialsProvider.toArray(CredentialsProvider[]::new);
        val remotes = this.gitInstance.getRepository().getRemoteNames();
        return !remotes.isEmpty() && this.gitInstance.pull()
            .setTimeout((int) timeoutInSeconds)
            .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
            .setRebase(false)
            .setTransportConfigCallback(this.transportConfigCallback)
            .setProgressMonitor(new LoggingGitProgressMonitor())
            .setCredentialsProvider(new ChainingCredentialsProvider(providers))
            .call()
            .isSuccessful();
    }

    @Override
    public void destroy() {
        if (this.gitInstance != null) {
            this.gitInstance.close();
        }
    }

    /**
     * The type Git object.
     */
    @Builder
    @Getter
    public static class GitObject {
        private final String content;

        private final TreeWalk treeWalk;

        private final ObjectId objectId;
    }
}
