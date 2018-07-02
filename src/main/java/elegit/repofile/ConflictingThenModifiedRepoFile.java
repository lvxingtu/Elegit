package elegit.repofile;

import elegit.Main;
import elegit.gui.PopUpWindows;
import elegit.models.RepoHelper;
import net.jcip.annotations.ThreadSafe;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A subclass of the RepoFile class that holds a reference to
 * and interacts with a file in the repository that was conflicting
 * but was then modified after the user was informed of the conflict
 *
 *  * This class is a view, controller, and model all mixed in one. That said. the model aspects are minimal, and the
 * view is mostly just a button and a context menu. Most notably, because most of the code is view oriented, ALL OF IT
 * should only be run from the JavaFX thread. In principle, a handful of method could be run elsewhere, but they're
 * fast anyway and not much would be gained; and most of this is FX work that should be done on the thread.
 *
 */
@ThreadSafe
// but only threadsafe because of the asserts on the FX thread nearly everywhere. No guarantees if any of those go;
// this needs to be thought through
public class ConflictingThenModifiedRepoFile extends RepoFile {

    private String resultType;

    private ConflictingThenModifiedRepoFile(Path filePath, RepoHelper repo) {
        super(filePath, repo);
        Main.assertFxThread();
        setTextIdTooltip("CONFLICTING\nMODIFIED","conflictingThenModifiedDiffButton",
        "This file was conflicting, but was recently modified.\nCommit if the changes are finalized.");
    }

    public ConflictingThenModifiedRepoFile(String filePathString, RepoHelper repo) {
        this(Paths.get(filePathString), repo);
        Main.assertFxThread();
    }

    @Override public boolean canAdd() throws GitAPIException {
        Main.assertFxThread();
        System.out.println(PopUpWindows.getAddingConflictingThenModifiedFileAlertShowing());
        if(!PopUpWindows.getAddingConflictingThenModifiedFileAlertShowing()) {
            resultType = PopUpWindows.showAddingConflictingThenModifiedFileAlert();
        } else {
            resultType = PopUpWindows.getResultType();
        }
        System.out.println(resultType);
        if (resultType.equals("add")) {
            return true;
        }
        return false;
    }

    @Override public boolean canRemove() { return true; }
}

