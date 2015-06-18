package edugit;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;

/**
 * A subclass of RepoFile that contains a file that Git reports as modified.
 */
public class ModifiedRepoFile extends RepoFile {
    public ModifiedRepoFile(String filePathString, Repository repo) {
        super(filePathString, repo);
    }

    public ModifiedRepoFile(Path filePath, Repository repo) {
        super(filePath, repo);
    }

    /**
     * When this RepoFile is checkboxed and the user commits, we add this file to the repository.
     *
     * @throws GitAPIException if the `git add` command fails.
     */
    @Override public void updateFileStatusInRepo() throws GitAPIException {
        // TODO: Unify this relativization! This code is copied from the SessionModel. Do things in one place only!
        // Relativize the path to the repository, because that's the file structure JGit
        //  looks for in an 'add' command
        Path repoDirectory = this.repo.getWorkTree().toPath();
        Path relativizedPath = repoDirectory.relativize(this.filePath);

        AddCommand add = new Git(this.repo).add().addFilepattern(relativizedPath.toString());
        add.call();
    }

    // TODO: modified icon instead of text
    @Override public String toString() {
        return "MODIFIED:" + super.toString();
    }
}