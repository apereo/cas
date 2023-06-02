package org.apereo.cas.git;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ChainingCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultGitRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultGitRepository implements GitRepository {
    private final Git gitInstance;

    @Getter
    private final List<CredentialsProvider> credentialsProvider;

    private final TransportConfigCallback transportConfigCallback;

    private final long timeoutInSeconds;

    private final boolean signCommits;

    private final boolean rebase;

    @Override
    public String getRepositoryRemote(final String name) {
        return gitInstance.getRepository().getRemoteName(name);
    }

    @Override
    public File getRepositoryDirectory() {
        return gitInstance.getRepository().getDirectory().getParentFile();
    }

    @Override
    public Collection<GitObject> getObjectsInRepository() throws Exception {
        return getObjectsInRepository(TreeFilter.ALL);
    }

    @Override
    public Collection<GitObject> getObjectsInRepository(final TreeFilter filter) throws Exception {
        val repository = gitInstance.getRepository();
        val head = repository.resolve(Constants.HEAD);

        LOGGER.debug("Head object id is [{}]", head.toObjectId().toString());
        try (val walk = new RevWalk(repository)) {
            val commit = walk.parseCommit(head);
            LOGGER.debug("Head commit id is [{}]", commit.getId());
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

    @Override
    public GitRepository.GitObject readObject(final TreeWalk treeWalk) throws Exception {
        try (val out = new ByteArrayOutputStream()) {
            val objectId = treeWalk.getObjectId(0);
            val repository = gitInstance.getRepository();
            val loader = repository.open(objectId);
            loader.copyTo(out);
            return GitRepository.GitObject.builder()
                .content(out.toString(StandardCharsets.UTF_8))
                .path(treeWalk.getPathString())
                .objectId(objectId)
                .build();
        }
    }

    @Override
    public void commitAll(final String message) throws Exception {
        gitInstance.add().addFilepattern(".").call();
        val config = gitInstance.getRepository().getConfig();
        val name = StringUtils.defaultIfBlank(config.getString("user", null, "name"), "CAS");
        val email = StringUtils.defaultIfBlank(config.getString("user", null, "email"), "cas@apereo.org");
        gitInstance.commit()
            .setMessage(message)
            .setAll(true)
            .setSign(signCommits)
            .setAuthor(name, email)
            .call();
    }

    @Override
    public void push() throws Exception {
        val remotes = gitInstance.remoteList().call();
        if (!remotes.isEmpty()) {
            val providers = credentialsProvider.toArray(CredentialsProvider[]::new);
            gitInstance.push()
                .setTimeout((int) timeoutInSeconds)
                .setTransportConfigCallback(transportConfigCallback)
                .setPushAll()
                .setCredentialsProvider(new ChainingCredentialsProvider(providers))
                .call();
        }
    }

    @Override
    public boolean pull() throws Exception {
        val providers = credentialsProvider.toArray(CredentialsProvider[]::new);
        val remotes = gitInstance.getRepository().getRemoteNames();

        if (remotes.isEmpty()) {
            LOGGER.debug("No remote repositories are specified to pull changes");
            return false;
        }

        return gitInstance.pull()
            .setTimeout((int) timeoutInSeconds)
            .setFastForward(MergeCommand.FastForwardMode.FF)
            .setRebase(rebase)
            .setTransportConfigCallback(transportConfigCallback)
            .setProgressMonitor(new LoggingGitProgressMonitor())
            .setCredentialsProvider(new ChainingCredentialsProvider(providers))
            .call()
            .isSuccessful();
    }

    @Override
    public void destroy() {
        if (gitInstance != null) {
            gitInstance.close();
        }
    }
}
