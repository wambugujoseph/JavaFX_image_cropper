package photo_editor.UI;

import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    @FXML
    private Button chooseMultImg;

    @FXML
    private Button cropAll;

    @FXML
    private TilePane workingArea;

    @FXML
    private Button openFolderOfImg;

    @FXML
    private Button removeAllImg;

    @FXML
    private Button chooseSaveLocation;

    @FXML
    private StackPane stackHolder;

    @FXML
    private TextField saveLocationTextField;


    private JFXSpinner spinner;
    private double imageListSize;

    private FileChooser fc;
    private DirectoryChooser dc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        //initiate all action events
        actionEventHandler();
    }

    /**
     * The method contains all the action events eg Button clicks
     */
    private void actionEventHandler() {
        //Select multiple images
        chooseMultImg.setOnAction(event -> chooseImagesToCrop());
        //Select cropped image save location
        chooseSaveLocation.setOnAction(event -> setChooseSaveLocation());
        //Open folder of image
        openFolderOfImg.setOnAction(event -> openFolderOfImages());
        //Remove all photos
        removeAllImg.setOnAction(event -> clearAllThePhotos());

        saveLocationTextField.textProperty().addListener(entry ->{
           ImageUtil.CROP_SAVE_DIR = saveLocationTextField.getText();
        });

    }

    /**
     * The method helps the user select multiple images
     * From a directory
     */
    private void chooseImagesToCrop() {

        //Expected file extension
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter(
                "Select Images", "*.jpg", "*.png", "*.jpeg", "*.JPG", "*.PNG", "*.JPEG");
        fc = new FileChooser();
        fc.getExtensionFilters().add(ef);
        List<File> files = fc.showOpenMultipleDialog(chooseMultImg.getScene().getWindow());
        showProgressIndicator();
        if (files != null && files.size() >0) {
            prepareImageForCropping(files);
        }

    }

    /**
     * The method help select the location where the cropped image will be saved
     */
    private void setChooseSaveLocation() {
        dc = new DirectoryChooser();
        File location = dc.showDialog(chooseSaveLocation.getScene().getWindow());
        if (location != null) {
            saveLocationTextField.setText(location.getAbsolutePath());
        }
    }

    /**
     * The method open a directory where the image to be cropped are
     * located
     */
    private void openFolderOfImages() {
        dc = new DirectoryChooser();
        File location = dc.showDialog(openFolderOfImg.getScene().getWindow());


        new Thread(()->{

            if (location != null &&location.isDirectory()) {
                Platform.runLater(this::showProgressIndicator);
                List<File>  fileList = Arrays.asList(Objects.requireNonNull(location.listFiles()));
                // System.out.println(location.getAbsolutePath());

                //Filter on the JPG and PNG images
                List<File> images =
                        fileList.stream()
                                .filter(File::isFile)
                                .filter(file ->
                                        file.getName().toLowerCase().endsWith(".png") ||
                                                file.getName().toLowerCase().endsWith(".jpg") ||
                                                file.getName().toLowerCase().endsWith(".jpeg"))
                                .collect(Collectors.toList());

                imageListSize = images.size();
                //Open the image for cropping
                prepareImageForCropping(images);
        }
        }).start();
    }

    /**
     * The method take in a list of images and then prepares the fro cropping
     * on the Grid cropping area
     *
     * @param imageToCrop ia list of image to be cropped
     */
    private void prepareImageForCropping(List<File> imageToCrop) {
        //Iterate the list of images
        double count = 0;
        for (File imageFile : imageToCrop) {
            //check if file is an image
            if (isFileImage(imageFile)){
                passImageToEditBoxController(imageFile);
            }
            count++;
            double finalCount = count;
            Platform.runLater(()->setSpinnerProgress(finalCount));
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        removeLoadImageProgress();
    }

    /**
     * The  Method help check is a file is an image of type JPG or PNG
     *
     * @param file is the file to check is the file is an image
     * @return true if the image is an image else returns false
     */
    private boolean isFileImage(File file) {
        if (file != null) {
            String filename = file.getName().toLowerCase();

            //Check if the file is a JPG or PNG
            if (filename.endsWith("jpg") || filename.endsWith("jpeg") || filename.endsWith("png")) {
                return true;
            }
        }
        return false;
    }

    /**
     * The Method pass and image as message to the EditBoxController before displaying it
     *
     * @param imageFile is the i mage file to pass to the EditBoxController
     */
    private void passImageToEditBoxController(File imageFile){
       Platform.runLater(()->{
           if(imageFile !=null){
               if (isFileImage(imageFile)){
                   try {
                       //Load the FXML edit Box
                       FXMLLoader loader = new FXMLLoader(getClass().getResource("editBox.fxml"));
                       AnchorPane root = loader.load();

                       EditBoxController editBox = loader.getController();
                       //pass image
                       editBox.imageToCropMessage(imageFile);

                       //show the edit Box
                       showEditBoxOnCroppingArea(root);

                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
       });
    }

    /**
     * The method displays the the EditBox pane on the cropping area (Grid pane)
     *
     * @param editBox the cropping pane that effects a single image
     */
    private void showEditBoxOnCroppingArea(AnchorPane editBox){
        workingArea.getChildren().add(editBox);
    }

    /**
     * The method clears the Image displayed for clopping
     */
    private void clearAllThePhotos(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to remove all the Photos");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK){
                int numOfNode = workingArea.getChildren().size();

                if(numOfNode > 0){
                    if (numOfNode ==1){
                        workingArea.getChildren().remove(0);
                    }else{
                        workingArea.getChildren().remove(0, numOfNode);
                    }
                }
            }
        });
    }

    private VBox getLoadImageProgress(){

        spinner = new JFXSpinner();

        VBox vBox = new VBox();
        vBox.setPrefSize(stackHolder.getPrefWidth(), stackHolder.getPrefHeight());
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-background-color: transparent");
        vBox.setOpacity(0.5);
        Label label = new Label("Please wait ...");
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: transparent; -fx-text-fill: #026602");

        spinner.setPrefSize(160, 160);
        spinner.setStyle("-fx-background-color: transparent");
        Shadow effect = new Shadow();
        effect.setBlurType(BlurType.GAUSSIAN);
        effect.setHeight(1.0);
        effect.setWidth(0);
        effect.setRadius(0);

        effect.setColor(Color.valueOf("#07ea35"));
        spinner.setEffect(effect);
        spinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        vBox.getChildren().addAll(spinner, label);

        return vBox;

    }

    /**
     *
     * @param progress double value between 0 and 1
     */
    public void setSpinnerProgress(double progress) {
        Platform.runLater(()->this.spinner.setProgress(progress/imageListSize));
    }


    private void showProgressIndicator(){
        stackHolder.getChildren().add(getLoadImageProgress());
    }

    private void removeLoadImageProgress(){
        Platform.runLater(()->{
            if (stackHolder.getChildren().size() >1) {
                stackHolder.getChildren().remove(1);
            }
        });
    }
}
