package edugit;

import java.util.ArrayList;

/**
 * Created by makik on 6/12/15.
 *
 * Subclass of CommitTreeModel that examines remote commits
 */
public class RemoteCommitTreeModel extends CommitTreeModel{

    public RemoteCommitTreeModel(SessionModel model, CommitTreePanelView view){
        super(model, view);
    }

    @Override
    public ArrayList<CommitHelper> getCommits(){
        if(this.model.currentRepoHelper != null){
            return this.model.currentRepoHelper.getLocalCommits(); // TODO: change to track remote commits
        }
        return new ArrayList<>();
    }
}