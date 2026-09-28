package javafx_archetype_simple.javafx;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Hyperlink;
/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();//esto obtiene la version de java y javafx
        var javafxVersion = SystemInfo.javafxVersion();
        //acá se crean las cosas
        VBox vbox = new VBox(); 
        VBox sub = new VBox(); 
        var label = new Label("version de java: "+ javaVersion +". Version de javafx" + javafxVersion + ".");
        TextField texto = new TextField("acá podés poner texto");
        CheckBox check = new CheckBox("esto es una checkbox");
        CheckBox otraCheck = new CheckBox("esto es otra checkbox");
        ToggleGroup grupoRadio = new ToggleGroup();
        RadioButton radio = new RadioButton("esto es un botón radio");
        RadioButton radioNo = new RadioButton("esto NO es un botón radio");
        Button boton = new Button("esto es un botón");
        TitledPane acor1 = new  TitledPane("este es el titulo del acordion", new Button("botón"));
        TitledPane acor2 = new  TitledPane("otra vez", new Button("botón"));
        ChoiceBox CBox = new ChoiceBox();
        CBox.getItems().addAll("elección1", "elección2");
        var cosas = new Label("cosas chistosas que encontré pero que no se si realmente nos vayan a servir");
        ColorPicker color = new ColorPicker();
        ProgressBar barra = new ProgressBar();
        Slider slider = new Slider(0, 1, 0.5);
        ScrollBar scroll = new ScrollBar();
        scroll.setOrientation(Orientation.HORIZONTAL);
        PasswordField contraseña = new PasswordField();//esto es para contraseñas
        contraseña.setText("contraseña");
        radio.setToggleGroup(grupoRadio);
        radioNo.setToggleGroup(grupoRadio);
        Hyperlink link = new Hyperlink("https://music.youtube.com/");
        Accordion acordion = new Accordion();
        acordion.getPanes().addAll(acor1,acor2);
        Spinner spinner = new Spinner(-10, 10, 0);
        VBox sub2 = new VBox();
        DatePicker fecha = new DatePicker();
        Menu menu = new Menu("esto es un menu");
        MenuBar menubar = new MenuBar(menu);

        //acá se agregan al vbox
        vbox.getChildren().add(boton);//se puede agregar de a uno
        vbox.getChildren().add(label);
        vbox.getChildren().addAll(check, otraCheck, texto, radio, radioNo,contraseña,slider);//se puede agregar de a varios
        vbox.getChildren().addAll(sub, sub2);
        sub.getChildren().addAll(acordion,cosas,color,barra,spinner,scroll, CBox); 
        sub2.getChildren().addAll(fecha,menubar,link);
        var scene = new Scene(vbox, 512, 512);//la pantalla
        stage.setTitle("ventana de prueba");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();//ejecuta
    }

}