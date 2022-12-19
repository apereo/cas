package org.apereo.cas.git;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link GitRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface GitRepository extends DisposableBean {

    /**
     * Gets credentials provider.
     *
     * @return the credentials provider
     */
    List<CredentialsProvider> getCredentialsProvider();

    /**
     * Gets repository directory.
     *
     * @return the repository directory
     */
    File getRepositoryDirectory();

    /**
     * Gets repository remote.
     *
     * @param name the name
     * @return the repository remote
     */
    String getRepositoryRemote(String name);

    /**
     * Gets objects in repository.
     *
     * @return the objects in repository
     * @throws Exception the exception
     */
    Collection<GitObject> getObjectsInRepository() throws Exception;

    /**
     * Gets objects in repository.
     *
     * @param filter the filter
     * @return the objects in repository
     * @throws Exception the exception
     */
    Collection<GitObject> getObjectsInRepository(TreeFilter filter) throws Exception;

    /**
     * Read object.
     *
     * @param treeWalk the tree walk
     * @return the git object
     * @throws Exception the exception
     */
    GitObject readObject(TreeWalk treeWalk) throws Exception;

    /**
     * Commit all.
     *
     * @param message the message
     * @throws Exception the exception
     */
    void commitAll(String message) throws Exception;

    /**
     * Push.
     *
     * @throws Exception the exception
     */
    void push() throws Exception;

    /**
     * Pull repository changes.
     *
     * @return true /false
     * @throws Exception the exception
     */
    boolean pull() throws Exception;

    /**
     * The type Git object.
     */
    @SuperBuilder
    @Getter
    class GitObject {
        private final String content;

        private final ObjectId objectId;

        private final String path;
    }
}
