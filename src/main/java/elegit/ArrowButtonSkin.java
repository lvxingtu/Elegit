package main.java.elegit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;

/**
 * Creates an arrow shape for a button based on the button's parameters.
 * Uses a label and a path with a fill to emulate the button's layout
 * and coloring. Supports arrows pointing in 0 to all 4 directions
 */
public class ArrowButtonSkin extends Group implements Skin<Button>{

    static final double ARROW_BASE_HEIGHT = 4;
    static final double ARROW_TIP_HEIGHT = 10;
    static final double ARROW_BASE_WIDTH_RATIO = 0.7;

    private enum ArrowDirection {UP, DOWN, LEFT, RIGHT}

    Button button;
    boolean up,down,left,right;

    public ArrowButtonSkin(Button button){
        this.button = button;

        this.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY){
                button.getOnAction().handle(new ActionEvent(button, event.getTarget()));
            }
        });

        this.button.textProperty().addListener((observable, oldValue, newValue) -> draw());
    }

    private void draw(){
        Label text = new Label(button.getText());
        text.getStyleClass().setAll(button.getStyleClass());
        text.setId(button.getId());

        Path path = new Path();
        text.backgroundProperty().addListener((observable, oldValue, newValue) -> {
            Paint color = newValue.getFills().get(0).getFill();
            path.setFill(color);
            path.setStroke(color);
        });

        button.getStyleClass().clear();
        getChildren().setAll(path, text);

        Platform.runLater(() -> {
            double labelWidth = text.getBoundsInLocal().getWidth();
            double labelHeight = text.getBoundsInLocal().getHeight();
            text.setTranslateX(button.getInsets().getLeft());
            text.setTranslateY(button.getInsets().getTop());

            // Create arrow button line path elements
            MoveTo startPoint = new MoveTo();
            double width = labelWidth + button.getInsets().getLeft();
            double height = labelHeight + button.getInsets().getBottom() + button.getInsets().getTop();
            startPoint.setX(0);
            startPoint.setY(0);
            path.getElements().add(startPoint);

            if(up){
                PathElement[] curves = getArrowSide(width, height, ArrowDirection.UP, true);
                path.getElements().add(curves[0]);
                path.getElements().add(curves[1]);
            }else{
                HLineTo topLine = new HLineTo(width);
                path.getElements().add(topLine);
            }

            if(right){
                PathElement[] curves = getArrowSide(width, height, ArrowDirection.RIGHT, true);
                path.getElements().add(curves[0]);
                path.getElements().add(curves[1]);
            }else{
                VLineTo rightLine = new VLineTo(height);
                path.getElements().add(rightLine);
            }

            if(down){
                PathElement[] curves = getArrowSide(width, height, ArrowDirection.DOWN, true);
                path.getElements().add(curves[0]);
                path.getElements().add(curves[1]);
            }else{
                HLineTo bottomLine = new HLineTo(0);
                path.getElements().add(bottomLine);
            }

            if(left){
                PathElement[] curves = getArrowSide(width, height, ArrowDirection.LEFT, true);
                path.getElements().add(curves[0]);
                path.getElements().add(curves[1]);
            }else{
                VLineTo leftLine = new VLineTo(0);
                path.getElements().add(leftLine);
            }
        });
    }

    private PathElement[] getArrowSide(double width, double height, ArrowDirection direction, boolean isConcave){
        return getArrowSide2(width, height, direction);
//        CubicCurveTo[] curve = new CubicCurveTo[2];
//        curve[0] = new CubicCurveTo();
//        curve[1] = new CubicCurveTo();
//        double controlX, controlY, x, y;
//
//        if(isConcave){
//            switch(direction){
//                case LEFT:
//                    // Bottom curve
//                    controlX = 0;
//                    controlY = height - ARROW_BASE_WIDTH_RATIO * height / 2;
//                    x = -ARROW_TIP_HEIGHT;
//                    y = height / 2;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX - ARROW_BASE_HEIGHT);
//                    curve[0].setControlY2(controlY);
//
//                    // Top curve
//                    controlX = 0;
//                    controlY = ARROW_BASE_WIDTH_RATIO * height / 2;
//                    x = 0;
//                    y = 0;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX - ARROW_BASE_HEIGHT);
//                    curve[1].setControlY1(controlY);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case RIGHT:
//                    // Top curve
//                    controlX = width;
//                    controlY = ARROW_BASE_WIDTH_RATIO * height / 2;
//                    x = width + ARROW_TIP_HEIGHT;
//                    y = height / 2;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX + ARROW_BASE_HEIGHT);
//                    curve[0].setControlY2(controlY);
//
//                    // Bottom curve
//                    controlX = width;
//                    controlY = height - ARROW_BASE_WIDTH_RATIO * height / 2;
//                    x = width;
//                    y = height;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX + ARROW_BASE_HEIGHT);
//                    curve[1].setControlY1(controlY);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case UP:
//                    // Left curve
//                    controlX = ARROW_BASE_WIDTH_RATIO * width / 2;
//                    controlY = 0;
//                    x = width / 2;
//                    y = -ARROW_TIP_HEIGHT;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX);
//                    curve[0].setControlY2(controlY - ARROW_BASE_HEIGHT);
//
//                    // Right curve
//                    controlX = width - ARROW_BASE_WIDTH_RATIO * width / 2;
//                    controlY = 0;
//                    x = width;
//                    y = 0;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX);
//                    curve[1].setControlY1(controlY - ARROW_BASE_HEIGHT);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case DOWN:
//                    // Right curve
//                    controlX = width - ARROW_BASE_WIDTH_RATIO * width / 2;
//                    controlY = height;
//                    x = width / 2;
//                    y = height + ARROW_TIP_HEIGHT;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX);
//                    curve[0].setControlY2(controlY + ARROW_BASE_HEIGHT);
//
//                    // Left curve
//                    controlX = ARROW_BASE_WIDTH_RATIO * width / 2;
//                    controlY = height;
//                    x = 0;
//                    y = height;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX);
//                    curve[1].setControlY1(controlY + ARROW_BASE_HEIGHT);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//            }
//        }else{
//            switch(direction){
//                case LEFT:
//                    // Bottom curve
//                    controlX = -ARROW_BASE_HEIGHT;
//                    controlY = height;
//                    x = -ARROW_TIP_HEIGHT;
//                    y = height / 2;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX);
//                    curve[0].setControlY2(controlY - ARROW_BASE_WIDTH_RATIO * height / 2);
//
//                    // Top curve
//                    controlX = -ARROW_BASE_HEIGHT;
//                    controlY = 0;
//                    x = 0;
//                    y = 0;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX);
//                    curve[1].setControlY1(controlY + ARROW_BASE_WIDTH_RATIO * height / 2);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case RIGHT:
//                    // Top curve
//                    controlX = width + ARROW_BASE_HEIGHT;
//                    controlY = 0;
//                    x = width + ARROW_TIP_HEIGHT;
//                    y = height / 2;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX);
//                    curve[0].setControlY2(controlY + ARROW_BASE_WIDTH_RATIO * height / 2);
//
//                    // Bottom curve
//                    controlX = width + ARROW_BASE_HEIGHT;
//                    controlY = height;
//                    x = width;
//                    y = height;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX);
//                    curve[1].setControlY1(controlY - ARROW_BASE_WIDTH_RATIO * height / 2);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case UP:
//                    // Left curve
//                    controlX = 0;
//                    controlY = -ARROW_BASE_HEIGHT;
//                    x = width / 2;
//                    y = -ARROW_TIP_HEIGHT;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX + ARROW_BASE_WIDTH_RATIO * width / 2);
//                    curve[0].setControlY2(controlY);
//
//                    // Right curve
//                    controlX = width;
//                    controlY = -ARROW_BASE_HEIGHT;
//                    x = width;
//                    y = 0;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX - ARROW_BASE_WIDTH_RATIO * width / 2);
//                    curve[1].setControlY1(controlY);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//                case DOWN:
//                    // Right curve
//                    controlX = width;
//                    controlY = height + ARROW_BASE_HEIGHT;
//                    x = width / 2;
//                    y = height + ARROW_TIP_HEIGHT;
//                    curve[0].setX(x);
//                    curve[0].setY(y);
//                    curve[0].setControlX1(controlX);
//                    curve[0].setControlY1(controlY);
//                    curve[0].setControlX2(controlX - ARROW_BASE_WIDTH_RATIO * width / 2);
//                    curve[0].setControlY2(controlY);
//
//                    // Left curve
//                    controlX = 0;
//                    controlY = height + ARROW_BASE_HEIGHT;
//                    x = 0;
//                    y = height;
//                    curve[1].setX(x);
//                    curve[1].setY(y);
//                    curve[1].setControlX1(controlX + ARROW_BASE_WIDTH_RATIO * width / 2);
//                    curve[1].setControlY1(controlY);
//                    curve[1].setControlX2(controlX);
//                    curve[1].setControlY2(controlY);
//                    break;
//            }
//        }
//        return curve;
    }

    private PathElement[] getArrowSide2(double width, double height, ArrowDirection direction){
        LineTo[] lines = new LineTo[2];
        lines[0] = new LineTo();
        lines[1] = new LineTo();
        switch(direction){
            case LEFT:
                lines[0].setX(-ARROW_TIP_HEIGHT);
                lines[0].setY(height / 2.);
                lines[1].setX(0);
                lines[1].setY(0);
                break;
            case RIGHT:
                lines[0].setX(width + ARROW_TIP_HEIGHT);
                lines[0].setY(height / 2.);
                lines[1].setX(width);
                lines[1].setY(height);
                break;
            case UP:
                lines[0].setX(width / 2.);
                lines[0].setY(-ARROW_TIP_HEIGHT);
                lines[1].setX(width);
                lines[1].setY(0);
                break;
            case DOWN:
                lines[0].setX(width / 2.);
                lines[0].setY(height + ARROW_TIP_HEIGHT);
                lines[1].setX(0);
                lines[1].setY(height);
                break;
        }
        return lines;
    }

    @Override
    public Button getSkinnable(){
        return button;
    }

    @Override
    public Node getNode(){
        return this;
    }

    @Override
    public void dispose(){
    }
}
