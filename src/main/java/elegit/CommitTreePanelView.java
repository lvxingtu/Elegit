package elegit;

import elegit.treefx.TreeGraph;
import elegit.treefx.TreeLayout;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

/**
 * Class for the local and remote panel views that handles the drawing of a tree structure
 * from a given treeGraph.
 *
 */
public class CommitTreePanelView extends Region{

    // Thread information
    public boolean isLayoutThreadRunning = false;
    private Task task;
    private Thread th;
    private String name;

    /**
     * Constructs a new view for the commit tree
     */
    public CommitTreePanelView(){
        super();
        this.setMinHeight(0);
    }

    /**
     * Helper method to initialize the commit tree scroll panes
     * @param treeGraph TreeGraph
     */
    private void initCommitTreeScrollPanes(TreeGraph treeGraph) {
        ScrollPane sp = treeGraph.getScrollPane();
        sp.setOnMouseClicked(event -> CommitTreeController.handleMouseClicked());
        getChildren().clear();
        getChildren().add(anchorScrollPane(sp));
        isLayoutThreadRunning = false;
    }

    /**
     * Handles the layout and display of the treeGraph. Creates a thread
     * in which to execute the TreeLayoutTask, and a thread that waits
     * for the layout to finish and then updates the view
     * @param treeGraph the graph to be displayed
     */
    synchronized void displayTreeGraph(TreeGraph treeGraph, CommitHelper commitToFocusOnLoad){
        if (Platform.isFxApplicationThread()) {
            initCommitTreeScrollPanes(treeGraph);
        }else {
            Platform.runLater(() -> initCommitTreeScrollPanes(treeGraph));
        }

        if(isLayoutThreadRunning){
            task.cancel();
            try{
                th.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        task = TreeLayout.getTreeLayoutTask(treeGraph);

        th = new Thread(task);
        th.setName("Graph Layout: "+this.name);
        th.setDaemon(true);
        th.start();
        isLayoutThreadRunning = true;

        Task<Void> endTask = new Task<Void>(){
            @Override
            protected Void call(){
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    isLayoutThreadRunning = false;
                }
                Platform.runLater(() -> CommitTreeController.focusCommitInGraph(commitToFocusOnLoad));
                return null;
            }
        };
        Thread endThread = new Thread(endTask);
        endThread.setName("Layout finalization");
        endThread.setDaemon(true);
        endThread.start();
    }

    /**
     * Displays an empty scroll pane
     */
    void displayEmptyView(){
        Platform.runLater(() -> {
            ScrollPane sp = new ScrollPane();
            this.getChildren().clear();
            this.getChildren().add(anchorScrollPane(sp));
        });
    }

    /**
     * Anchors the width and height of the scroll pane to the width and height of
     * the view to ensure the scroll pane expands appropriately on resize
     * @param sp the scrollpane to anchor
     * @return the passed in scrollpane after being anchored
     */
    private ScrollPane anchorScrollPane(ScrollPane sp){
        sp.prefWidthProperty().bind(this.widthProperty());
        sp.prefHeightProperty().bind(this.heightProperty());
        return sp;
    }

    /**
     * Sets the name of this view, which appears in the Threads spawned by it
     * @param name the name
     */
    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
