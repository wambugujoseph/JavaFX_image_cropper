package photo_editor.UI;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;


class ResizableRectangle extends Rectangle {

    private double rectangleStartX;
    private double rectangleStartY;
    private double mouseClickPozX;
    private double mouseClickPozY;
    private static final double RESIZER_SQUARE_SIDE = 8;
    private Paint resizerSquareColor = Color.WHITE;
    private Paint rectangleStrokeColor = Color.BLACK;

    private double prefMaxWidthLim;
    private double prefMaxHeightLim;
    private double allowableMovingW;
    private double allowableMovingH;

    ResizableRectangle(double x, double y, double width, double height, Group group) {
        super(x, y, width, height);
        group.getChildren().add(this);
        super.setStroke(rectangleStrokeColor);
        super.setStrokeWidth(1);
        super.setFill(Color.color(1, 1, 1, 0));

        // set the max width of the rectangle
        setPrefMaxWidthLim(width);
        //set the max prf height of the rectangle
        setPrefMaxHeightLim(height);

        Rectangle moveRect = new Rectangle(0, 0, 0, 0);
        moveRect.setFill(Color.color(1, 1, 1, 0));
        moveRect.xProperty().bind(super.xProperty());
        moveRect.yProperty().bind(super.yProperty());
        moveRect.widthProperty().bind(super.widthProperty());
        moveRect.heightProperty().bind(super.heightProperty());

        group.getChildren().add(moveRect);

        moveRect.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                moveRect.getParent().setCursor(Cursor.HAND));

        moveRect.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            moveRect.getParent().setCursor(Cursor.MOVE);
            mouseClickPozX = event.getX();
            mouseClickPozY = event.getY();

        });

        moveRect.addEventHandler(MouseEvent.MOUSE_RELEASED, event ->
                moveRect.getParent().setCursor(Cursor.HAND));

        moveRect.addEventHandler(MouseEvent.MOUSE_EXITED, event ->
                moveRect.getParent().setCursor(Cursor.DEFAULT));

        moveRect.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {

            double offsetX = event.getX() - mouseClickPozX;
            double offsetY = event.getY() - mouseClickPozY;
            double newX = super.getX() + offsetX;
            double newY = super.getY() + offsetY;

            if (newX >= 0 && newX + super.getWidth() <= super.getParent().getBoundsInLocal().getWidth()) {
                super.setX(newX);
            }

            if (newY >= 0 && newY + super.getHeight() <= super.getParent().getBoundsInLocal().getHeight()) {
                super.setY(newY);
            }
            mouseClickPozX = event.getX();
            mouseClickPozY = event.getY();

        });


        makeNWResizerSquare(group);
        makeCWResizerSquare(group);
        makeSWResizerSquare(group);
        makeSCResizerSquare(group);
        makeSEResizerSquare(group);
        makeCEResizerSquare(group);
        makeNEResizerSquare(group);
        makeNCResizerSquare(group);

        XYLimitControl();


    }

    private void setPrefMaxWidthLim(double prefMaxWidthLim) {
        this.prefMaxWidthLim = prefMaxWidthLim;
    }

    private void setPrefMaxHeightLim(double prefMaxHeightLim) {
        this.prefMaxHeightLim = prefMaxHeightLim;
    }

    private void makeNWResizerSquare(Group group) {
        Rectangle squareNW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

        squareNW.xProperty().bind(super.xProperty().subtract(squareNW.widthProperty().divide(2.0)));
        squareNW.yProperty().bind(super.yProperty().subtract(squareNW.heightProperty().divide(2.0)));
        group.getChildren().add(squareNW);

        squareNW.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareNW.getParent().setCursor(Cursor.NW_RESIZE));

        prepareResizerSquare(squareNW);

        squareNW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            rectangleStartY = super.getY();
            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;
            double newX = super.getX() + offsetX;
            double newY = super.getY() + offsetY;

            if (newX >= 0 && newX <= super.getX() + super.getWidth()) {
                super.setX(newX);
                super.setWidth(super.getWidth() - offsetX);
            }

            if (newY >= 0 && newY <= super.getY() + super.getHeight()) {
                super.setY(newY);
                setSquaredSize(super.getHeight() - offsetY);
            }

        });
    }

    private void makeCWResizerSquare(Group group) {
        Rectangle squareCW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareCW.xProperty().bind(super.xProperty().subtract(squareCW.widthProperty().divide(2.0)));
        squareCW.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(
                squareCW.heightProperty().divide(2.0))));
        group.getChildren().add(squareCW);

        squareCW.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareCW.getParent().setCursor(Cursor.W_RESIZE));

        prepareResizerSquare(squareCW);

        squareCW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            double offsetX = event.getX() - rectangleStartX;
            double newX = super.getX() + offsetX;

            if (newX >= 0 && newX <= super.getX() + super.getWidth() - 5) {
                super.setX(newX);
                setSquaredSize(super.getWidth() - offsetX);

            }

        });

    }

    private void makeSWResizerSquare(Group group) {
        Rectangle squareSW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSW.xProperty().bind(super.xProperty().subtract(squareSW.widthProperty().divide(2.0)));
        squareSW.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSW.heightProperty().divide(2.0))));
        group.getChildren().add(squareSW);

        squareSW.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareSW.getParent().setCursor(Cursor.SW_RESIZE));

        prepareResizerSquare(squareSW);

        squareSW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            rectangleStartY = super.getY();
            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;
            double newX = super.getX() + offsetX;

            if (newX >= 0 && newX <= super.getX() + super.getWidth() - 5) {
                super.setX(newX);
                super.setWidth(super.getWidth() - offsetX);
            }

            if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
                setSquaredSize(offsetY);
            }
        });
    }

    private void makeSCResizerSquare(Group group) {
        Rectangle squareSC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

        squareSC.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(
                squareSC.widthProperty().divide(2.0))));
        squareSC.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSC.heightProperty().divide(2.0))));
        group.getChildren().add(squareSC);

        squareSC.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareSC.getParent().setCursor(Cursor.S_RESIZE));

        prepareResizerSquare(squareSC);

        squareSC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartY = super.getY();
            double offsetY = event.getY() - rectangleStartY;

            if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
                setSquaredSize(offsetY);
            }

        });
    }

    private void makeSEResizerSquare(Group group) {
        Rectangle squareSE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareSE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareSE.widthProperty().divide(2.0)));
        squareSE.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(
                squareSE.heightProperty().divide(2.0))));
        group.getChildren().add(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareSE.getParent().setCursor(Cursor.SE_RESIZE));

        prepareResizerSquare(squareSE);

        squareSE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            rectangleStartY = super.getY();
            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;

            if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
                super.setWidth(offsetX);
            }

            if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
                setSquaredSize(offsetY);
            }
        });
    }

    private void makeCEResizerSquare(Group group) {
        Rectangle squareCE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        squareCE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareCE.widthProperty().divide(2.0)));
        squareCE.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(
                squareCE.heightProperty().divide(2.0))));
        group.getChildren().add(squareCE);

        squareCE.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareCE.getParent().setCursor(Cursor.E_RESIZE));

        prepareResizerSquare(squareCE);

        squareCE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            double offsetX = event.getX() - rectangleStartX;
            if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
                setSquaredSize(offsetX);
            }

        });
    }

    private void makeNEResizerSquare(Group group) {
        Rectangle squareNE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

        squareNE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(
                squareNE.widthProperty().divide(2.0)));
        squareNE.yProperty().bind(super.yProperty().subtract(squareNE.heightProperty().divide(2.0)));
        group.getChildren().add(squareNE);

        squareNE.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareNE.getParent().setCursor(Cursor.NE_RESIZE));

        prepareResizerSquare(squareNE);

        squareNE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartX = super.getX();
            rectangleStartY = super.getY();
            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;
            double newY = super.getY() + offsetY;

            if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
                super.setWidth(offsetX);
            }

            if (newY >= 0 && newY <= super.getY() + super.getHeight() - 5) {
                super.setY(newY);
                setSquaredSize(super.getHeight() - offsetY);
            }

        });
    }

    private void makeNCResizerSquare(Group group) {
        Rectangle squareNC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

        squareNC.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(
                squareNC.widthProperty().divide(2.0))));
        squareNC.yProperty().bind(super.yProperty().subtract(
                squareNC.heightProperty().divide(2.0)));
        group.getChildren().add(squareNC);

        squareNC.addEventHandler(MouseEvent.MOUSE_ENTERED, event ->
                squareNC.getParent().setCursor(Cursor.N_RESIZE));

        prepareResizerSquare(squareNC);

        squareNC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            rectangleStartY = super.getY();
            double offsetY = event.getY() - rectangleStartY;
            double newY = super.getY() + offsetY;

            if (newY >= 0 && newY <= super.getY() + super.getHeight()) {
                super.setY(newY);
                setSquaredSize(super.getHeight() - offsetY);
            }

        });
    }

    private void prepareResizerSquare(Rectangle rect) {
        rect.setFill(resizerSquareColor);

        rect.addEventHandler(MouseEvent.MOUSE_EXITED, event ->
                rect.getParent().setCursor(Cursor.DEFAULT));
    }

    /**
     * Since the ID card require squared passport size photo
     * the setSquaredSize method is used to perform this activity
     * And is called by all the methods that alter the size of the crop rectangle
     * The method also limit prevent the rectangle for exceeding  preferred crop size
     *
     * @param size represent the height and width of the crop rectangle
     */
    private void setSquaredSize(double size) {

        if (size < prefMaxWidthLim) { // limit height from exceeding the max height to help maintain CROP RATIO
            super.setHeight(size);
        } else {
            super.setHeight(prefMaxHeightLim);
        }


        if (size < prefMaxWidthLim) { // limit width from exceeding the max width to help maintain CROP RATIO
            super.setWidth(size);
        } else {
            super.setHeight(prefMaxWidthLim);
        }

    }

    private void XYLimitControl() {

        //X axis movement constraint
        super.xProperty().addListener(e -> {
            //System.out.println("X = " + super.getX());
            //condition one
            if (super.getX() + super.getWidth() > allowableMovingW){
                //subtract to avoid X moving out of the raster region
                super.setX(allowableMovingW - super.getWidth() -2);
            }

            // condition two
            if(super.getX() <=1){
                super.setX(1);
            }
        });

        // Y axis movement constraint
        super.yProperty().addListener(e -> {
           // System.out.println("Y = " + super.getY());
            //condition one
            if (super.getY() + super.getHeight() > allowableMovingH){
                //subtract to avoid X moving out of the raster region
                super.setY(allowableMovingH - super.getHeight() -3);
            }

            // condition two
            if(super.getY() <=1){
                super.setY(1);
            }
        });
    }

    public void setAllowableMovingArea(double allowableMovingW, double allowableMovingH) {
        this.allowableMovingW = allowableMovingW;
        this.allowableMovingH = allowableMovingH;
    }


}