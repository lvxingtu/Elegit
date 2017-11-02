package elegit.treefx;

import elegit.Main;
import elegit.models.CommitHelper;
import elegit.models.RefHelper;
import elegit.models.RepoHelper;
import elegit.models.SessionModel;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ContextMenu;
import org.apache.http.annotation.ThreadSafe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Thanks to RolandC for providing the base graph code structure:
 * http://stackoverflow.com/questions/30679025/graph-visualisation-like-yfiles-in-javafx/30696075#30696075
 *
 * The underlying model of a tree graph represented with generational cells and directed edges between
 * them.
 *
 * Note that this isn't really a model in the sense of the rest of the project, but it's sort of a model relative
 * to the TreeGraph in that is stores data for that TreeGraph. Depends on your perspective.
 */
@ThreadSafe
// but critically only because of all the asserts requiring this be done only in the FX thread. Without that, it
// isn't threadsafe. It seems like this class should be able to live outside of the FX thread, at least for some
// of its work, but it's too tightly connected to Cell and Edge. Cell in particular maintains data on which cells
// it connects to. That's not "view"-like at all, but it's in there, and tightly integrated.
public class TreeGraphModel {

    private final List<Cell> allCells;
    private final List<Cell> addedCells;
    private final List<Cell> removedCells;

    private final List<Edge> addedEdges;
    private final List<Edge> removedEdges;

    // Map of each cell's id to the cell itself
    private final Map<String,Cell> cellMap;

    // Whether this graph has been through the layout process already or not
    private boolean firstTimeLayoutFlag;

    // Last repo displayed, so can determine if doing the same one again or not
    private Path pathOfRepoLastDisplayed;

    // A list of cells in this graph that do not have the default shape
    private List<Cell> cellsWithNonDefaultShapesOrLabels;

    // Updated every time merge is called to hold the number of cells present
    // This is not private so other aspects of the view can bind it. Don't touch it unless on FX thread!
    final IntegerProperty numCellsProperty = new SimpleIntegerProperty();

    /**
     * Constructs a new model for a tree graph.
     * Resets and creates the cell and edge lists, as well as the cell map
     */
    public TreeGraphModel() {
        Main.assertFxThread();
        allCells = new ArrayList<>();
        addedCells = new ArrayList<>();
        removedCells = new ArrayList<>();

        addedEdges = new ArrayList<>();
        removedEdges = new ArrayList<>();

        cellMap = new HashMap<>(); // <id,cell>
        firstTimeLayoutFlag = false;
        cellsWithNonDefaultShapesOrLabels = new ArrayList<>();

        pathOfRepoLastDisplayed = Paths.get("");
    }

    public synchronized boolean checkAndFlipTreeLayoutDoneAtLeastOnce() {
        Main.assertFxThread();
        Path currentPath = SessionModel.getSessionModel().getCurrentRepoHelper().getLocalPath();
        if (!pathOfRepoLastDisplayed.equals(currentPath)) {
            pathOfRepoLastDisplayed = currentPath;
            return false;
        }
        return true;
    }


    /**
     * @param id the id to check
     * @return whether this graph contains the given id or not
     */
    public boolean containsID(String id){
        Main.assertFxThread();
        return cellMap.containsKey(id);
    }

    public Cell getCell(String id) {
        Main.assertFxThread();
        return cellMap.get(id);
    }

    /**
     * @return the cells added since the last update
     */
    public List<Cell> getAddedCells() {
        Main.assertFxThread();
        return Collections.unmodifiableList(new ArrayList<>(addedCells));
    }

    /**
     * @return the cells removed since the last update
     */
    public List<Cell> getRemovedCells() {
        Main.assertFxThread();
        return Collections.unmodifiableList(new ArrayList<>(removedCells));
    }

    /**
     * @return the edges added since the last update
     */
    public List<Edge> getAddedEdges() {
        Main.assertFxThread();
        return Collections.unmodifiableList(new ArrayList<>(addedEdges));
    }
    /**
     * @return the edges removed since the last update
     */
    public List<Edge> getRemovedEdges() {
        Main.assertFxThread();
        List<String> removedMap = new ArrayList<>();
        for (Cell c : removedCells)
            removedMap.add(c.getCellId());
        List<Edge> oldRemoved = new ArrayList<>();
        oldRemoved.addAll(removedEdges);

        // If there are edges going to a cell being replaced, keep those
        for (Edge e : oldRemoved) {
            // Check that the old parent is being replaced and child is staying
            if (cellMap.containsKey(e.getSource().getCellId())
                    && removedMap.contains(e.getSource().getCellId())
                    && cellMap.containsKey(e.getTarget().getCellId())
                    && !removedMap.contains(e.getTarget().getCellId())) {
                // Make a replacement edge if it doesn't already exist
                addEdge(e.getSource().getCellId(), e.getTarget().getCellId());

            }
        }
        return Collections.unmodifiableList(new ArrayList<>(removedEdges));
    }

    /**
     * Adds a new cell with the given ID, time, and labels to the tree whose
     * parents are the cells with the given IDs.
     * @param contextMenu the context contextMenu that will appear when right clicking on a cell
     * @param parentIds the IDs of the parents of the new cell, if any
     * @param type the type of the cell, local, remote, or both
     */
    public void addCell(CommitHelper commitToAdd, ContextMenu contextMenu,
                        List<String> parentIds, Cell.CellType type){
        Main.assertFxThread();
        String newId = commitToAdd.getName();
        long time = commitToAdd.getWhen().getTime();
        RepoHelper repo = SessionModel.getSessionModel().getCurrentRepoHelper();
        String displayLabel = repo.getCommitDescriptorString(commitToAdd, false);
        List<RefHelper> refs = repo.getRefsForCommit(commitToAdd);
        // Create a list of parents
        List<Cell> parents = new ArrayList<>();
        for (String parentId : parentIds) {
            parents.add(cellMap.get(parentId));
        }
        parents = Collections.unmodifiableList(parents);

        Cell cell;
        switch(type) {
            case LOCAL:
                cell = new Cell(newId, time, parents, Cell.CellType.LOCAL);
                break;
            case REMOTE:
                cell = new Cell(newId, time, parents, Cell.CellType.REMOTE);
                break;
            case BOTH:
            default:
                cell = new Cell(newId, time, parents, Cell.CellType.BOTH);
                break;
        }
        setCellLabels(cell, displayLabel, refs);
        cell.setContextMenu(contextMenu);
        addCell(cell);

        // Note: a merge can be the result of any number of commits if it
        // is an octopus merge, so we add edges to all of them
        for (String parentId : parentIds)
            this.addEdge(parentId, newId);
    }


    /**
     * Adds a cell to both the addedCells list and the cell map, and removes
     * any cell with a conflicting ID
     * @param cell the cell to add
     */
    private void addCell(Cell cell) {
        Main.assertFxThread();
        if(cellMap.containsKey(cell.getCellId())){
            Cell oldCell = cellMap.remove(cell.getCellId());
            for(Cell p : cell.getCellParents()){
                p.removeCellChild(oldCell);
            }
            removedCells.add(oldCell);
            this.removeEdges(oldCell);
        }

        addedCells.add(cell);
        cellMap.put(cell.getCellId(), cell);
    }

    /**
     * Adds an edge between the two cells corresponding to the given
     * IDs
     * @param sourceId the parent cell
     * @param targetId the child cell
     */
    public void addEdge(String sourceId, String targetId) {
        Main.assertFxThread();
        Cell sourceCell = cellMap.get(sourceId);
        Cell targetCell = cellMap.get(targetId);

        addEdge(sourceCell, targetCell);

    }

    /**
     * Adds an edge between two cells
     * @param source the source (parent) cell
     * @param target
     */
    public void addEdge(Cell source, Cell target) {
        Main.assertFxThread();
        Edge edge = new Edge(source, target);
        source.addEdge(edge);
        target.addEdge(edge);

        addedEdges.add(edge);
    }

    /**
     * Queues a cell to be removed by removing it from the cell map and
     * putting it into the removed cells list
     *
     * @param id the cell id to remove
     */
    public void removeCell(String id) {
        Main.assertFxThread();
        Cell cell = cellMap.get(id);
        if(cell != null && cellMap.containsKey(cell.getCellId())){
            Cell oldCell = cellMap.remove(cell.getCellId());
            for(Cell p : cell.getCellParents()){
                p.removeCellChild(oldCell);
            }
            removedCells.add(oldCell);
            this.removeEdges(oldCell);
        }
    }

    /**
     * Removes all edges connected to the given cell
     * @param cell the cell whose edges will be removed
     */
    private void removeEdges(Cell cell){
        Main.assertFxThread();
        List<Edge> oldEdges = cell.getEdges();

        for(Edge e : oldEdges){
            removedEdges.add(e);
            e.getTarget().removeEdge(e);
            e.getSource().removeEdge(e);
        }
    }

    /**
     * Sets the label for the cell with the given ID to be the given string
     * @param cellId the id of the cell to label
     * @param commitDescriptor the new commit descriptor
     * @param refs the branch names to include on the label
     */
    public void setCellLabels(String cellId, String commitDescriptor, List<RefHelper> refs){
        Main.assertFxThread();
        setCellLabels(cellMap.get(cellId), commitDescriptor, refs);
    }

    /**
     * Sets the labels for a given cell
     * @param cell the cell to set labels for
     * @param commitDescriptor the labels to put on the cell
     * @param refs the list of refs to add
     */
    private void setCellLabels(Cell cell, String commitDescriptor, List<RefHelper> refs){
        Main.assertFxThread();
        cell.setLabels(commitDescriptor, refs);
        if(refs.size() > 0) cellsWithNonDefaultShapesOrLabels.add(cell);
    }

    public void setCurrentCellLabels(String cellId, Set<String> refs) {
        Main.assertFxThread();
        setCurrentCellLabels(cellMap.get(cellId), refs);
    }

    public void setCurrentCellLabels(Cell cell, Set<String> refs) {
        Main.assertFxThread();
        cell.setCurrentLabels(refs);
    }

    public void setLabelMenus(String cellId, Map<RefHelper, ContextMenu> menuMap) {
        Main.assertFxThread();
        cellMap.get(cellId).setLabelMenus(menuMap);
    }

    public void setRemoteBranchCells(String cellId, List<String> remoteBranches) {
        Main.assertFxThread();
        cellMap.get(cellId).setRemoteLabels(remoteBranches);
    }

    /**
     * Sets the shape of the cell with the given ID to be the given shape.
     * If the shape is not the default shape, adds it to the list of non-
     * default shaped cells.
     * @param cellId the id of the cell to label
     * @param shape the new shape
     */
    public void setCellShape(String cellId, CellShape shape){
        Main.assertFxThread();
        Cell cell = cellMap.get(cellId);
        cell.setShape(shape);
        cellsWithNonDefaultShapesOrLabels.add(cell);
    }

    /**
     * Sets the type of the cell with the given ID to be the given type.
     * If the type isn't the default (CellType.BOTH), adds it to the list
     * of non-default cells.
     * @param cellId the id of the cell to type
     * @param type the new type
     */
    public void setCellType(String cellId, Cell.CellType type) {
        Main.assertFxThread();
        Cell cell = cellMap.get(cellId);
        if (cell == null)
            return;
        cell.setCellType(type);
        if (type != Cell.CellType.BOTH)
            cellsWithNonDefaultShapesOrLabels.add(cell);
    }

    /**
     * Changes every cell back to the default shape
     * @return a list of CellIDs corresponding to the cells that were changed
     */
    public List<String> resetCellShapes(){
        Main.assertFxThread();
        List<String> resetIDs = new ArrayList<>();
        for(Cell cell : cellsWithNonDefaultShapesOrLabels){
            cell.setShapeDefault();
            String id = cell.getCellId();
            if(!resetIDs.contains(id) && allCells.contains(cell)) resetIDs.add(id);
        }
        cellsWithNonDefaultShapesOrLabels = new ArrayList<>();
        return Collections.unmodifiableList(resetIDs);
    }

    /**
     * Updates the lists for added and removed cells, leaving the tree
     * completely updated
     */
    public void merge() {
        Main.assertFxThread();
        // cells
        allCells.addAll(addedCells);
        allCells.removeAll(removedCells);

        addedCells.clear();
        removedCells.clear();

        addedEdges.clear();
        removedEdges.clear();

        numCellsProperty.set(allCells.size());
    }

    public List<Cell> getAllCells() {
        Main.assertFxThread();
        return new ArrayList<>(allCells);
    }
}