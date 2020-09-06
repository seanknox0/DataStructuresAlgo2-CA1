package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class Controller {

    @FXML
    ImageView origImage, triImage, cellImage;
    @FXML
    MenuBar menuBar;
    @FXML
    Slider cellSize;
    @FXML
    Label countEst, countEst2;

    public static Image img;
    public static int h;
    public static int w;
    public static int pixArray[];
    public static WritableImage holderImg;
    public static HashSet<Integer> rootTracker;
    public static HashSet<Integer> rootGreaterThanSliderTracker;

    public void exit() {
        Platform.exit();
    }

    public void uploadImage() {
        FileChooser fc = new FileChooser();
        Window imageWindow = null;
        File selectedFile = fc.showOpenDialog(imageWindow);

        //img = new Image(selectedFile.toURI().toString());
        img = new Image(selectedFile.toURI().toString(), origImage.getFitWidth(), origImage.getFitHeight(), false,true);
        h = (int) img.getHeight();
        w = (int) img.getWidth();
        pixArray = new int [h * w];
        rootTracker = new HashSet<>(pixArray.length);
        rootGreaterThanSliderTracker = new HashSet<>(pixArray.length);

        origImage.setImage(img);
    }


    public void convertTricolour() {
        PixelReader pr = img.getPixelReader();
        WritableImage tri = new WritableImage((int) img.getWidth(), (int) img.getHeight());
        PixelWriter pw = tri.getPixelWriter();

        for (int i = 0; i < (int) img.getHeight(); i++) {
            for (int j = 0; j < (int) img.getWidth(); j++) {
                Color color = pr.getColor(j, i);
                if (color.getRed() > 0.72 & color.getBlue() > 0.72 & color.getGreen() > 0.72) {
                    pw.setColor(j, i, Color.WHITE);
                } else if (color.getRed() > color.getBlue()) {
                    pw.setColor(j, i, Color.RED);
                } else if (color.getBlue() > color.getRed()) {
                    pw.setColor(j, i, Color.PURPLE);
                }
            }
        }
        triImage.setImage(tri);
        holderImg = tri;
        unionFindOnPixels();
    }

    public void unionFindOnPixels() {
        WritableImage newImg = copyImage(holderImg);
        PixelReader pr = newImg.getPixelReader();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Color color = pr.getColor(j, i);
                if (color.equals(Color.WHITE)) {
                    pixArray[(i * w) + j] = -1;
                } else {
                    pixArray[(i * w) + j] = (i * w) + j;
                }
            }
        }

        //////////////////////////////
        //Quick Union Implementation//
        //////////////////////////////

        for (int i = 0; i < pixArray.length; i++) {
            if (pixArray[i] != -1) {
                if (((i + 1) < pixArray.length) && (pixArray[i + 1] != -1) && (((i+1) % w) != 0)) {
                    UnionFind.union(pixArray, i, i + 1);
                }
                if (((i + w) < pixArray.length) && (pixArray[i + w] != -1)) {
                    UnionFind.union(pixArray, i, i + w);
                }
            }
        }

        triImage.setImage(newImg);
        holderImg = newImg;

        //PRINTING
        for (int i = 0; i < pixArray.length; i++) {
            if (i % w == 0)
                System.out.println();
            System.out.print(UnionFind.find(pixArray, i) + " ");
        }
    }

    public void noiseReduction() {
        WritableImage noiseImg = copyImage(holderImg);//SR changed uses method below
        PixelWriter pw = noiseImg.getPixelWriter();//SR added
        int wid = (int)noiseImg.getWidth(); //SR added
        rootTracker.clear(); //SR added, cleared everytime this method is called
        rootGreaterThanSliderTracker.clear();
        int pixCount[] = new int[pixArray.length];
        //SETTING ALL ELEMENTS TO 0
        for(int i = 0; i < pixCount.length; i++) {
            pixCount[i] = 0;
        }
        //SETTING PIXEL AMOUNTS FOR EACH ROOT AND ADD ROOT TO HASHSET
        for(int i = 0; i < pixArray.length; i++) {
            if (pixArray[i] != -1) {
                int loc = UnionFind.find(pixArray, i);
                pixCount[loc] += 1;

            }
        }
        //SR: added as you can't add to rootTracker until all root counts have been added to pixCount
        for(int i= 0;i<pixCount.length;i++){
            if(pixCount[i]>0 && cellSize.getValue() >= pixCount[i]) {
                rootTracker.add(i);
            }
            else if(pixCount[i]>0 && cellSize.getValue() < pixCount[i])
                rootGreaterThanSliderTracker.add(i);
        }

        for(int i=0;i<pixArray.length;i++){
            if(rootTracker.contains(UnionFind.find(pixArray, i))){
                int col=i%wid, row=i/wid;
                pw.setColor(col, row, Color.WHITE);
            }
        }

        triImage.setImage(noiseImg);
        holderImg = noiseImg;
    }


    /**
     * copy the given image to a writeable image
     * copied from StackOverFlow
     * @param image
     * @return a writeable image
     */
    public static WritableImage copyImage(Image image) {
        int height=(int)image.getHeight();
        int width=(int)image.getWidth();
        PixelReader pixelReader=image.getPixelReader();
        WritableImage writableImage = new WritableImage(width,height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    public void idRectangle(){
        WritableImage rectImg = copyImage(holderImg);
        cellImage.setImage(rectImg);
        int top, bottom, left, right;
        for(int i:rootGreaterThanSliderTracker){
            top = h;
            bottom = 0;
            left = w;
            right = 0;
            for(int j = 0; j < pixArray.length; j++){
                if(pixArray[j] != -1 && UnionFind.find(pixArray, j) == i) {
                    int x = j%w;
                    int y = j/w;
                    if(x<left) left = x;
                    if(x>right) right = x;
                    if(y<top) top = y;
                    if(y>bottom) bottom = y;
                }
            }
            Rectangle r = new Rectangle(left, top, right-left, bottom-top);
            r.setTranslateX(cellImage.getLayoutX());
            r.setTranslateY(cellImage.getLayoutY());
            r.setStroke(Color.BLACK);
            r.setFill(Color.TRANSPARENT);
            ((Pane)cellImage.getParent()).getChildren().add(r);
            holderImg = rectImg;
        }
    }

    public void countEstimate() {
        WritableImage countImg = copyImage(holderImg);
        PixelReader pr = countImg.getPixelReader();
        HashSet<Integer> overallC = new HashSet<>();
        HashSet<Integer> whiteC = new HashSet<>();
        for (int j = 0; j < pixArray.length; j++) {
            if (pixArray[j] != -1) { //I.e. not a white cell
                overallC.add(UnionFind.find(pixArray, j));
            }
            if (pixArray[j] != -1 && (pr.getColor() == Color.PURPLE)) { //I.e. not a white cell
                whiteC.add(UnionFind.find(pixArray, j));
            }
        }
        countEst.setText("" + overallC.size());
        countEst2.setText("" + whiteC.size());
    }


    public void initialize() {

    }
}
