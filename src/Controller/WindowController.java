package Controller;

import Model.FilterInterface.AbstractSinglePixelFilterModel.FilterImpl.NegativeFilter;
import Model.FilterInterface.AbstractSinglePixelFilterModel.FilterImpl.CustomFilter;
import Model.FilterInterface.AbstractSinglePixelFilterModel.FilterImpl.SepiaFilter;
import Model.FilterInterface.Filter;
import Model.Validator.IntegerValidator;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ForkJoinPool;

public class WindowController {

    @FXML
    private Button loadBtn;

    @FXML
    private TextField newFilterSizeTxt;

    @FXML
    private GridPane matrixGridPane;

    @FXML
    private ChoiceBox<String> filterChoiceBox;

    @FXML
    private ImageView originalImage;

    @FXML
    private ImageView filteredImage;

    @FXML
    private Label sizeLabel;

    @FXML
    private TextField thresholdLabel;

    @FXML
    private Label processingTimeLabel;

    @FXML
    private Label tasksLabel;

    @FXML
    private TextField parallelismLevelTxt;

    @FXML
    private TextField loadTxt;

    @FXML
    private HBox RGBBox;

    @FXML
    private Slider redSlider;

    @FXML
    private Slider greenSlider;

    @FXML
    private Slider blueSlider;

    @FXML
    private Button resetButton;

    private ForkJoinPool forkJoinPool = new ForkJoinPool();

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public void setForkJoinPool() {
        this.forkJoinPool = new ForkJoinPool(Integer.parseInt(parallelismLevelTxt.getText()));
    }

    @FXML
    public void initialize(){

        loadBtn.setText("Load");
        newFilterSizeTxt.setText("3");
        filterChoiceBox.getItems().setAll("Custom","Negative","Sepia");
        filterChoiceBox.getSelectionModel().selectFirst();
        filterChoiceBox.setOnAction(this::toggleMatrixEnable);
        sizeLabel.setText("Size: " + (int) originalImage.getImage().getWidth() + "x" + (int) originalImage.getImage().getHeight() + "px");
        newFilterSizeTxt.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 3, new IntegerValidator("-?([1-5])?")));
        parallelismLevelTxt.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(),
                        forkJoinPool.getParallelism(),
                        new IntegerValidator("-?^([1-9]|[1-2][0-9]{0,4}|3[0-9]{0,3}|3[0-1][0-9]{0,3}|32[0-6][0-9]{0,2}|327[0-5][0-9]|3276[0-7])?")));
//        filteredImage.getImage().getPixelReader().get
//        try {l
//            String absolute = new File("@../../resources/eif.jpg").getCanonicalPath();
//            System.out.println(absolute);
////            printNewImage(new Image(absolute));
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null, "Cannot load init image");
//        }
    }

    @FXML
    public void resetSliders(){
        redSlider.setValue(0);
        greenSlider.setValue(0);
        blueSlider.setValue(0);
    }

    @FXML
    public void changeFilterMatrix(){
        boolean notNum = false;
        String str = newFilterSizeTxt.getText();
        if(str.equals("") || str.isEmpty()){
            notNum = true;
        }

        if(!notNum){
            int n = Integer.parseInt(newFilterSizeTxt.getText());
            int txtSize = 25;
            int margin = 2;

            matrixGridPane.getChildren().clear();

            matrixGridPane.setPrefSize(n*(txtSize+margin)+margin, n*(txtSize+margin)+margin);
            matrixGridPane.setMinSize(n*(txtSize+margin)+margin, n*(txtSize+margin)+margin);
            matrixGridPane.setMaxSize(n*(txtSize+margin)+margin, n*(txtSize+margin)+margin);

            matrixGridPane.getColumnConstraints().clear();
            matrixGridPane.getRowConstraints().clear();

            for(int i = 0 ; i<n ; i++) {
                for (int j = 0; j < n; j++) {
                    TextField tf = new TextField();
                    tf.setText("3");
                    tf.setPrefSize(txtSize, txtSize);
                    tf.setMaxSize(txtSize, txtSize);
                    tf.setMinSize(txtSize, txtSize);
                    tf.setFont(Font.font(10));
                    tf.setAlignment(Pos.CENTER);
                    tf.setStyle("-fx-background-color: #6699cc;");
                    GridPane.setConstraints(tf, i, j);
                    GridPane.setMargin(tf, new Insets(2, 2, 2, 2));
                    matrixGridPane.getChildren().add(tf);
                }
            }
        }
    }

    @FXML
    private void toggleMatrixEnable(ActionEvent actionEvent){
        if(filterChoiceBox.getSelectionModel().getSelectedIndex() != 0){
            matrixGridPane.setDisable(true);
            newFilterSizeTxt.setDisable(true);
            RGBBox.setDisable(true);
        }
        else {
            matrixGridPane.setDisable(false);
            newFilterSizeTxt.setDisable(false);
            RGBBox.setDisable(false);
        }
    }

    @FXML
    public void openFileChooser(){
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("C:\\Users\\Ozii\\Desktop"));
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File selectedFile = fc.showOpenDialog(null);

        if(selectedFile != null) {
            loadTxt.setText(selectedFile.getAbsolutePath());
            printNewImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void searchImageByPath(){
        try {
            Image image = new Image(new FileInputStream(loadTxt.getText()));
            printNewImage(image);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Could not find image");
        }
    }

    private  void printNewImage(Image image){
        originalImage.setImage(image);
    }

    @FXML
    public void filter(){

        // counter of recursive actions (tasks)
        FilterManager.i = 0;
        ForkJoinPool fjp = getForkJoinPool();
        int pool = Integer.parseInt(parallelismLevelTxt.getText());
        if(fjp.getParallelism() != pool){
            fjp = new ForkJoinPool(pool);
        }


        BufferedImage bi = SwingFXUtils.fromFXImage(originalImage.getImage(),null);
        String filterName = filterChoiceBox.getSelectionModel().getSelectedItem();
        Filter filter;
        switch (filterName){
            case "Negative":
                filter = new NegativeFilter();
                break;
            case "Sepia":
                filter = new SepiaFilter();
                break;
            case "Custom":
                int[] addRgb = {(int)redSlider.getValue(), (int)greenSlider.getValue(), (int)blueSlider.getValue()};
//                RGBBox.getChildren();
                filter = new CustomFilter(addRgb);
                break;
            default:
                filter = new NegativeFilter();
        }

        long beginT = System.nanoTime();
        FilterManager fm = new FilterManager(bi, filter, 0, (int)originalImage.getImage().getWidth(), 0, (int)originalImage.getImage().getHeight(), Integer.parseInt(thresholdLabel.getText()));
        fjp.invoke(fm);
        filteredImage.setImage(SwingFXUtils.toFXImage(bi, null));
        tasksLabel.setText("Tasks: " + FilterManager.i);
        long endT = System.nanoTime();

        processingTimeLabel.setText("Processing time: "+(endT-beginT)/1000000+"ms");
    }
}
