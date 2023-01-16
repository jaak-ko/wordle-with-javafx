package fi.tuni.prog3.wordle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Wordle extends Application {

    private int activeBox = 0;
    private ArrayList<TextField> boxes;
    private String currentWord;
    private Label infoBox;
    private Button startBtn;
    private ArrayList<String> words;
    private boolean lastBoxFilled;
    String prematureEnter = "  Give a complete word before pressing Enter!";
    
    int gridXDim = 500;
    int gridYDim = 500;
    int boxSide = 72;

    @Override
    public void start(Stage stage) throws IOException {

        getWords("words.txt");

        this.startBtn = new Button("NEW\nGAME");
        this.startBtn.setFont(Font.font("Monospaced", 18));
        this.startBtn.setStyle("-fx-border-style: solid;" +
                               "-fx-border-radius: 5;");                  
        this.startBtn.setId("newGameBtn");
        this.startBtn.setMaxWidth(this.boxSide);
        this.startBtn.setPrefHeight(this.boxSide);
        this.startBtn.setTextAlignment(TextAlignment.CENTER);;

        this.startBtn.setBackground(
            new Background(
            new BackgroundFill(Color.GREY, new CornerRadii(7), null)));
        this.startBtn.setOnAction((event) -> {newGame(stage);});
        this.startBtn.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode().equals(KeyCode.ENTER)){
                    newGame(stage);
                }
            }});
        
        this.infoBox = new Label("");
        this.infoBox.setId("infoBox");
        this.infoBox.setFont(Font.font("Monospaced"));
        stage.setTitle("WORDLE");
        
        newGame(stage);
    }


    private void getWords(String fileName) 
    throws IOException {
        
        this.words = new ArrayList<>();
        var rdr = new BufferedReader(new FileReader(fileName));
        String line = "";
        while ((line = rdr.readLine()) != null) {
            this.words.add(line.strip().toUpperCase());
        }

        rdr.close();
        return;
    }

    public void runGame() {

        boxes.get(activeBox).setStyle(
            "-fx-border-style: solid;" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 5"
            );

        boxes.get(activeBox).requestFocus();
    }

    public void newGame(Stage stage) {

        this.boxes = new ArrayList<>();
        this.activeBox = 0;
        this.boxes.clear();
        this.infoBox.setText("");
        this.infoBox.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();

        grid.setStyle("-fx-background-color: grey;");
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(grid, this.gridXDim, this.gridYDim);
        grid.add(this.startBtn, 3, 0);
        grid.add(this.infoBox, 1, 1, 5, 1);

        Random intGen = new Random();
        this.currentWord = this.words.get(intGen.nextInt(this.words.size()));
        
        grid.add(new Label(), 0, 1);
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < this.currentWord.length(); x++) {
                
                TextField f = new TextField("");
                f.setId(String.format("%d_%d", y, x));
                f.setPrefSize(this.boxSide, this.boxSide);
                f.setAlignment(Pos.CENTER);
                f.setFont(Font.font("Verdana", FontWeight.BOLD, 27));
                f.setStyle(
                    "-fx-border-color: black;" +
                    "-fx-border-style: dashed;" +
                    "-fx-border-radius: 7;"
                    );
                f.setBackground(new Background(
                    new BackgroundFill(Color.GREY, new CornerRadii(7), null)));
                grid.add(f, x+1, y+2);
                this.boxes.add(f);

                // text uppercase
                f.setTextFormatter(new TextFormatter<>((change) -> {
                    change.setText(change.getText().toUpperCase());
                    return change;
                }));

                // only one character allowed
                f.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(final ObservableValue<? extends String> 
                    ov, final String oldValue, final String newValue) {
                        if (f.getText().length() > 0 && !oldValue.equals("")) {
                            String s = f.getText().substring(0, 1);
                            f.textProperty().set("");;
                            f.textProperty().set(oldValue);
                        }
                    }
                });


                if (x == 0) {
                    f.setOnKeyReleased(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {

                        setInfoLabel("");

                        char[] letters = e.getText().toCharArray();
                        char letter = Character.toUpperCase(letters[0]);

                        if (e.getCode().equals(KeyCode.ENTER)) {
                            setInfoLabel(prematureEnter);
                        }
                        
                        else {
                            if (letter >= 'A' && letter <= 'Z'){
                            f.textProperty().set(e.getText());
                            nextBox();
                            }
                            else {
                                f.textProperty().set("");
                            }
                        }
                        
                    }});
                }
                else if ((x + 1) % this.currentWord.length() == 0) {
                    f.setOnKeyReleased(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent e) {
                       
                            if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                                if (!lastBoxFilled){
                                    deletePrevious();
                                }
                                else {
                                    deleteCurrent();
                                    f.textProperty().set("");
                                    lastBoxFilled = false;
                                }
                                    
                            }
                            else if (e.getCode().equals(KeyCode.ENTER)) {
                                if (f.getText().length() > 0) {
                                    setInfoLabel("");
                                    wordGuessed();
                                    lastBoxFilled = false;
                                }
                                else {
                                    setInfoLabel(prematureEnter);
                                }
                            }
                            else {
                                char[] letters = e.getText().toCharArray();
                                char letter = Character.toUpperCase(letters[0]);

                                if (letter >= 'A' && letter <= 'Z'){
                                    f.textProperty().set(e.getText());
                                    lastBoxFilled = true;
                                    }
                                    else {
                                        f.textProperty().set("");
                                    }

                            }
                        }});
                }
                else {
                    f.setOnKeyReleased(new EventHandler<KeyEvent>() {
                        @ Override
                        public void handle(KeyEvent e) {
                            
                            if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                                    deletePrevious();
                            }
                            else if (e.getCode().equals(KeyCode.ENTER)) {
                                setInfoLabel(prematureEnter);
                                
                            }
                            else {
                                char[] letters = e.getText().toCharArray();
                                char letter = Character.toUpperCase(letters[0]);
                            
                            
                                if (letter >= 'A' && letter <= 'Z'){
                                    f.textProperty().set(e.getText());
                                    nextBox();
                                }  
                                else {
                                    f.textProperty().set("");
                                }
                            }
                            
                        }});
                }
            }
        }
        
        stage.setScene(scene);
        stage.show();

        runGame();

    }
    
    public void wordGuessed() {
        char[] letters = this.currentWord.toCharArray();
        int wordIdx = 0;
        for (int i = this.activeBox - this.currentWord.length() + 1; i <= this.activeBox ; i++) {
            if (this.boxes.get(i).getText().equals(String.valueOf(letters[wordIdx]))) {
                this.boxes.get(i).setBackground(new Background(
                    new BackgroundFill(Color.GREEN, new CornerRadii(7), null)));
                this.boxes.get(i).setStyle(
                    "-fx-border-color: black;" +
                    "-fx-border-style: solid;" +
                    "-fx-border-radius: 7");
            }
            else if(this.currentWord.contains(this.boxes.get(i).getText())) {
                this.boxes.get(i).setBackground(new Background(
                    new BackgroundFill(Color.ORANGE, new CornerRadii(7), null)));
                this.boxes.get(i).setStyle(
                    "-fx-border-color: black;" +
                    "-fx-border-style: solid;" +
                    "-fx-border-radius: 7" 
                    );
            }
            else {
                this.boxes.get(i).setBackground(new Background(
                    new BackgroundFill(Color.GRAY, new CornerRadii(7), null)));
                this.boxes.get(i).setStyle(
                    "-fx-border-color: black;" +
                    "-fx-border-style: solid;" +
                    "-fx-border-radius: 7");
            }
            wordIdx += 1;
        }

        if (checkIfWin()) {
            this.infoBox.setText("          CONGRATULATIONS, YOU WON!");

        }
        else if (checkIfLose()) {
            this.infoBox.setText("              GAME OVER, YOU LOST");
        }
        else {
            this.activeBox += 1;

            this.boxes.get(this.activeBox).setStyle(
                "-fx-border-color: black;" +
                "-fx-border-style: solid;" +
                "-fx-border-radius: 7"
                );
            this.boxes.get(this.activeBox).requestFocus();
        }
    }

    public boolean checkIfLose() {
        this.startBtn.requestFocus();
        if (this.activeBox + 1 < this.boxes.size()) {
            return false;
        }

        this.startBtn.requestFocus();
        return true;

    }

    public boolean checkIfWin() {
        int wordIdx = 0;
        char[] letters = this.currentWord.toCharArray();
        for (int i = this.activeBox - this.currentWord.length()+1; i <= this.activeBox ; i++) {
            if (! this.boxes.get(i).getText().equals(String.valueOf(letters[wordIdx]))) {
                return false;
            }   
            wordIdx += 1;
        }
        
        this.startBtn.requestFocus();
        return true;
    }

    public void nextBox() {
        this.boxes.get(this.activeBox).setStyle(
            "-fx-border-color: black;" +
            "-fx-border-style: dashed;" +
            "-fx-border-radius: 7;"
            );

        this.activeBox += 1;

        this.boxes.get(this.activeBox).setStyle(
            "-fx-border-color: black;" +
            "-fx-border-style: solid;" +
            "-fx-border-radius: 7;"
            );
        this.boxes.get(this.activeBox).requestFocus();
    }

    public void deletePrevious() {
        this.boxes.get(this.activeBox).setStyle(
            "-fx-border-color: black;" +
            "-fx-border-style: dashed;" +
            "-fx-border-radius: 7;");

        this.activeBox -= 1;
        this.boxes.get(this.activeBox).textProperty().set("");
        this.boxes.get(this.activeBox).setStyle(
            "-fx-border-color: black;" +
            "-fx-border-radius: 7;" +
            "-fx-border-style: solid;");
        this.boxes.get(this.activeBox).requestFocus();

    }

    public void deleteCurrent() {
        this.boxes.get(this.activeBox).textProperty().set("");
    }

    public void setInfoLabel(String prompt) {
        this.infoBox.setText(prompt);
    }



    public static void main(String[] args) 
    throws IOException {

        launch(new String[] {});
        
    }
}