package elegit;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Subclass of CommitTreeModel that examines remote commits
 */
public class RemoteCommitTreeModel extends CommitTreeModel{

    public final static String REMOTE_TREE_VIEW_NAME = "Remote commit tree";

    public RemoteCommitTreeModel(SessionModel model, CommitTreePanelView view){
        super(model, view);
        this.view.setName(REMOTE_TREE_VIEW_NAME);
    }

    @Override
    protected List<CommitHelper> getAllCommits(RepoHelper repoHelper) {
        return repoHelper.getRemoteCommits();
    }

    @Override
    protected List<BranchHelper> getAllBranches(RepoHelper repoHelper){
        return repoHelper.getBranchModel().getBranchListUntyped(BranchModel.BranchType.REMOTE);
    }
}