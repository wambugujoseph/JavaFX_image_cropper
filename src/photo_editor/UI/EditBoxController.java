package photo_editor.UI;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.geometry.Bounds;
import javafx.scene.Group;

import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import photo_editor.App;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditBoxController implements Initializable {

    @FXML
    private HBox image_area;

    @FXML
    private TextField fileName;

    @FXML
    private Button cropBtn;

    @FXML
    private Label cropXY;

    @FXML
    private Label cropImgHW;

    private ImageView mainImageView;
    private Image mainImage;
    private double newWidth;
    private double newHeight;
    private boolean isAreaSelected = false;

    private double widthZoomRatio;
    private double heightZoomRatio;
    private File originalImageFile;
    private FXMLLoader editBoxLoader;

    final AreaSelection areaSelection = new AreaSelection();
    final Group selectionGroup = new Group();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Tooltip tooltip = new Tooltip();
        fileName.setTooltip(tooltip);
        //prevent unwanted character on the file name
        fileName.textProperty().addListener((obs, oldText, newText) -> {
            if ((newText.charAt(newText.length() - 1) + "").matches("[\\\\/:*?\"<>|]")) {
                fileName.setText(oldText);
            }
        });
    }

    /**
     * The method is called when the image is ready for cropping
     *
     * @param bounds    is the cropping area bounds
     * @param imageView holds the original image
     * @return true for successful cropping
     */
    private boolean cropImage(Bounds bounds, ImageView imageView) {

        int width = (int) (bounds.getWidth() * widthZoomRatio);
        int height = (int) (bounds.getHeight() * heightZoomRatio);

        int x = (int) (bounds.getMinX() * widthZoomRatio);
        int y = (int) (bounds.getMinY() * heightZoomRatio);

        String filName = this.fileName.getText();

        if (!filName.isEmpty() && isCorrectImageName(filName.toLowerCase())) {
            String storePath = ImageUtil.CROP_SAVE_DIR + "/" + this.fileName.getText();

            return crop(x, y, width, height, originalImageFile, storePath, getFileExtension(originalImageFile.getName()));

        } else {
            new Alert(Alert.AlertType.ERROR, "Invalid file name! ").show();
        }
        return false;
    }

    /**
     * The method crops and image
     *
     * @param x              coordinate
     * @param y              coordinate
     * @param width          of the image
     * @param height         of the image
     * @param imageToCrop    Image file to be cropped
     * @param outPutFilePath cropped image filePath
     * @param formatName     File extension "JPG", "PNG" ...
     * @return true is the image is croped
     */
    public boolean crop(int x, int y, int width, int height, File imageToCrop, String outPutFilePath, String formatName) {
        try {

            BufferedImage originalImage = ImageIO.read(imageToCrop);

            BufferedImage subImage = originalImage.getSubimage(x, y, width, height);

            File outPutFile = new File(outPutFilePath);

            ImageIO.write(subImage, formatName, outPutFile);

            return true;
        } catch (RasterFormatException e){
            new Alert(Alert.AlertType.ERROR, e.getMessage()+
                    "\nThe error is cased by:" +
                    "\n  Cropping window is outside the image").show();
            e.printStackTrace();
        }catch(IOException e){
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            e.printStackTrace();

        }
        return false;
    }

    /**
     * The method extracts file extension from the file name
     *
     * @param filename is the file name to to find the extension
     * @return the file extension eg "jpg", "png"
     */
    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1)).orElse(".jpg");
    }

    /**
     * @param filename the name to be given to cropped image in lowercase
     * @return true is valid file name else return false
     */
    private boolean isCorrectImageName(String filename) {

        if (filename.endsWith(".png") || filename.endsWith(".jpeg") || filename.endsWith(".jpg")) {
            return true;
        }
        return false;
    }

    /**
     * The method sets the file image that is to be cropped
     * Is is mostly called within other controllers passing the image file as message to the
     * EditBoxController which is to be seen whe the EditBox is displayed
     *
     * @param file is the image file that is to be cropped
     */
    public void imageToCropMessage(File file) {

        if (file != null) {

            originalImageFile = file;
            mainImageView = new ImageView();

        /*
            crop button action event handling
         */
            cropBtn.setOnAction(event -> {
                boolean isCropped= invokeCrop();
                if (isCropped){
                     Controller controller = App.loader.getController();
                     controller.removeEditBox(editBoxLoader);
                }

            });

            selectionGroup.getChildren().add(mainImageView);

            AnchorPane pane = new AnchorPane();
            pane.setMaxWidth(newWidth);
            pane.setMaxHeight(newHeight);
            pane.setPrefSize(newWidth, newHeight);
            pane.setStyle("-fx-border-color: red");
            pane.getChildren().add(selectionGroup);

            image_area.getChildren().add(pane);

            mainImage = convertFileToImage(file);

            //Set the file name
            fileName.setText(file.getName());

            /*
             * Convert the image to fit the display section
             * by altering the ratios */
            double ratio = mainImage.getWidth() / mainImage.getHeight();
            double viewPortH = image_area.getPrefHeight();
            newWidth = viewPortH * ratio;
            newHeight = viewPortH;
            mainImageView.setFitHeight(viewPortH);
            mainImageView.setFitWidth(newWidth);
            //mainImageView.setPreserveRatio(true);

        /*
            Calculate the shrink ratios
         */
            widthZoomRatio = mainImage.getWidth() / newWidth;
            heightZoomRatio = mainImage.getHeight() / newHeight;


            //Add the image to the display area
            mainImageView.setImage(mainImage);

            areaSelection.selectArea(selectionGroup);
        }
    }


    public void setEditBoxLoader(FXMLLoader loader){
        this.editBoxLoader = loader;
    }

    private void clearSelection(Group group) {
        //deletes everything except for base container layer
        isAreaSelected = false;
        group.getChildren().remove(1, group.getChildren().size());

    }

    public boolean invokeCrop(){
        //System.out.println(areaSelection.selectArea(selectionGroup).getBoundsInParent().getWidth());
        if (isAreaSelected) {
           return cropImage(areaSelection.selectArea(selectionGroup).getBoundsInParent(), mainImageView);
        }
        return  false;
    }

    private Image convertFileToImage(File imageFile) {
        Image image = null;
        try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
            image = new Image(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * @return the longer side of the displayed image
     */
    private double getShortSideOfImage() {

        double h = mainImageView.getFitHeight();
        double w = mainImageView.getFitWidth();
        return Math.min(h, w); //Return the smaller value
    }

    /**
     * @return true if the height is the longest side
     */
    private boolean isWidthLongest() {
        return mainImageView.getFitHeight() < mainImageView.getFitWidth();
    }

    public Button getCropBtn() {
        return cropBtn;
    }

    public class AreaSelection {
        private Group group;

        private ResizableRectangle selectionRectangle = null;
        private double rectangleStartX;
        private double rectangleStartY;
        private Paint darkAreaColor = Color.color(0, 0, 0, 0.5);

        private ResizableRectangle selectArea(Group group) {
            this.group = group;

            // group.getChildren().get(0) == mainImageView. We assume image view as base container layer
            if (mainImageView != null && mainImage != null) {
                //this.group.getChildren().get(0).addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
                this.group.getChildren().get(0).addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
                this.group.getChildren().get(0).addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
            }

            //position and dimensions
            if (!isAreaSelected) {
                if (isWidthLongest()) {
                    int x = (int) ((mainImageView.getFitWidth() - getShortSideOfImage()) / 2);
                    if (x < 1) x = 1;
                    selectionRectangle = new ResizableRectangle(x, 1, getShortSideOfImage() - 2, getShortSideOfImage() - 2, group);
                } else {
                    int y = (int) ((mainImageView.getFitHeight() - getShortSideOfImage()) / 2);
                    if (y < 1) y = 1;
                    selectionRectangle = new ResizableRectangle(1, y, getShortSideOfImage() - 2, getShortSideOfImage() - 2, group);
                }

                //Set the allowable movement rectangle of the cropping rectangle
                selectionRectangle.setAllowableMovingArea(mainImageView.getFitWidth(), mainImageView.getFitHeight());

                isAreaSelected = true;
                displayImageDimensions();
                darkenOutsideRectangle(selectionRectangle);
            }
            return selectionRectangle;
        }

        EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {
            if (event.isSecondaryButtonDown())
                return;

            double offsetX = event.getX() - rectangleStartX;
            double offsetY = event.getY() - rectangleStartY;

            /*
                X -axis drag bounds
             */
            if (offsetX > 0) { // Not move on the negative on the x axis
                if (event.getX() > newWidth/*mainImage.getWidth()*/)
                    selectionRectangle.setWidth(/*mainImage.getWidth()*/newWidth - rectangleStartX);
                else {
                    selectionRectangle.setWidth(offsetX);
                }
            } else {
                if (event.getX() < 0)
                    selectionRectangle.setX(0);
                else if (event.getX() < newWidth) {
                    selectionRectangle.setX(event.getX());
                }
                selectionRectangle.setWidth(rectangleStartX - selectionRectangle.getX());
            }

            if (offsetY > 0) {
                if (event.getY() < newHeight/*mainImage.getHeight()*/)
                    selectionRectangle.setHeight(/*mainImage.getHeight()*/newHeight - rectangleStartY);
                else
                    selectionRectangle.setHeight(offsetY);
            } else {
                if (event.getY() < 0)
                    selectionRectangle.setY(0);
                else
                    selectionRectangle.setY(event.getY());
                selectionRectangle.setHeight(rectangleStartY - selectionRectangle.getY());
            }
            displayImageDimensions();
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
            if (selectionRectangle != null)
                isAreaSelected = true;
            assert selectionRectangle != null;
            //darkenOutsideRectangle(selectionRectangle);
        };


        /**
         * The method performs event listen to the manipulation on crop are and cropping
         * rectangle and then performs an update to the UI
         */
        private void displayImageDimensions() {
            cropImgHW.setText("[" + ((int) selectionRectangle.getWidth()) +
                    "*" + ((int) selectionRectangle.getHeight()) + "]");
            cropXY.setText("[" + ((int) selectionRectangle.getX()) + ", " + ((int) selectionRectangle.getY()) + "]");

            // width listener that responds to the change in width
            selectionRectangle.widthProperty().addListener(x -> {
                cropImgHW.setText("[" + ((int) selectionRectangle.getWidth()) +
                        "*" + ((int) selectionRectangle.getHeight()) + "]");
            });

            selectionRectangle.xProperty().addListener(event -> {
                cropXY.setText("[" + ((int) selectionRectangle.getX()) + ", " + ((int) selectionRectangle.getY()) + "]");
            });

            selectionRectangle.yProperty().addListener(event -> {
                cropXY.setText("[" + ((int) selectionRectangle.getX()) + ", " + ((int) selectionRectangle.getY()) + "]");
            });


        }


        private void darkenOutsideRectangle(javafx.scene.shape.Rectangle rectangle) {
            javafx.scene.shape.Rectangle darkAreaTop = new javafx.scene.shape.Rectangle(0, 0, darkAreaColor);
            javafx.scene.shape.Rectangle darkAreaLeft = new javafx.scene.shape.Rectangle(0, 0, darkAreaColor);
            javafx.scene.shape.Rectangle darkAreaRight = new javafx.scene.shape.Rectangle(0, 0, darkAreaColor);
            javafx.scene.shape.Rectangle darkAreaBottom = new Rectangle(0, 0, darkAreaColor);

            darkAreaTop.widthProperty().bind(mainImageView.fitWidthProperty());
            darkAreaTop.heightProperty().bind(rectangle.yProperty());

            darkAreaLeft.yProperty().bind(rectangle.yProperty());
            darkAreaLeft.widthProperty().bind(rectangle.xProperty());
            darkAreaLeft.heightProperty().bind(rectangle.heightProperty());

            darkAreaRight.xProperty().bind(rectangle.xProperty().add(rectangle.widthProperty()));
            darkAreaRight.yProperty().bind(rectangle.yProperty());
            darkAreaRight.widthProperty().bind(mainImageView.fitWidthProperty().subtract(
                    rectangle.xProperty().add(rectangle.widthProperty())));
            darkAreaRight.heightProperty().bind(rectangle.heightProperty());

            darkAreaBottom.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty()));
            darkAreaBottom.widthProperty().bind(mainImageView.fitWidthProperty());
            darkAreaBottom.heightProperty().bind(mainImageView.fitHeightProperty().subtract(
                    rectangle.yProperty().add(rectangle.heightProperty())));

            // adding dark area rectangles before the selectionRectangle. So it can't overlap rectangle
            group.getChildren().add(1, darkAreaTop);
            group.getChildren().add(1, darkAreaLeft);
            group.getChildren().add(1, darkAreaBottom);
            group.getChildren().add(1, darkAreaRight);

            // make dark area container layer as well
            //darkAreaTop.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaTop.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            darkAreaTop.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

            //darkAreaLeft.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaLeft.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            darkAreaLeft.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

            //darkAreaRight.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaRight.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            darkAreaRight.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

            //darkAreaBottom.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            darkAreaBottom.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            darkAreaBottom.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
        }
    }
}
