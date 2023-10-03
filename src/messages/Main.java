/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messages;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author mprad
 */



public class Main extends Application implements Initializable {

    private String user;
    private Channel canal;
    private Scene scene;
    private HashMap<String, ObservableList<String>> listeMessages;
    private String currentDestino;
    private ObservableList<String> listeAmis;

    @FXML
    private TextArea message;
    @FXML
    private ListView liste;
    @FXML
    private ListView listeUtilisateurs;
    @FXML
    private TextField nouveauUtilisateur;
    @FXML
    private Label monUtilisateur;
    @FXML
    private Button EnvoyerButton;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader f = new FXMLLoader(getClass().getResource("Main.fxml"));
        f.setController(this);
        scene = new Scene(f.load(), 800, 400);
        primaryStage.setTitle("Chat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        liste.setDisable(true);
        message.setDisable(true);
        EnvoyerButton.setDisable(true);
        listeMessages = new HashMap<>();
        listeAmis = FXCollections.observableArrayList();
        user = FxDialogs.showTextInput("Username", "Nom", "user1");
        if (user == null) {
            Platform.exit();
            return;
        }
        monUtilisateur.setText("Utilisateur " + user + " connecté.");
        try {
            canal = new ConnectionFactory().newConnection().createChannel();
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-single-active-consumer", true);
            DeclareOk cola = canal.queueDeclare(user, true, false, false, arguments); //durable
            if (cola.getConsumerCount() != 0) {
                FxDialogs.showError("Error", "connecté déjà.", user);
                Platform.exit();
                return;
            }
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String recibido = new String(delivery.getBody(), "UTF-8");
                System.out.println("Reçu '" + recibido + "'");
                String emisor = recibido.split("##")[0];
                if (!listeMessages.containsKey(emisor)) {
                    Platform.runLater(() -> listeAmis.add(emisor));
                    listeMessages.put(emisor, FXCollections.observableArrayList());
                }
                Platform.runLater(() -> listeMessages.get(emisor).add(recibido.split("##")[1]));
            };
            canal.basicConsume(user, true, deliverCallback, (c) -> {
            });
        } catch (IOException | TimeoutException ex) {
            try {
                canal.queueDelete(user);
            } catch (IOException ex1) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        message.setOnKeyPressed((KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                enviarmessage(message.getText());
                message.setText("");
            }
        });
        listeUtilisateurs.setItems(listeAmis);
        listeUtilisateurs.setOnMouseClicked((MouseEvent event) -> {
            if (listeUtilisateurs.getSelectionModel().getSelectedItem() != null) {
                setDestino(listeUtilisateurs.getSelectionModel().getSelectedItem().toString());
            }
        });
        liste.setCellFactory(param -> new ListCell<String>() {
            @Override
            public void updateItem(String name, boolean empty) {
                Platform.runLater(() -> {
                    super.updateItem(name, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        if (name.startsWith("&!&")) {
                            this.setAlignment(Pos.CENTER_RIGHT);
                            setText(name.substring(3));
                        } else {
                            this.setAlignment(Pos.CENTER_LEFT);
                            setText(name);
                        }
                    }
                });
            }
        }
        );
    }

    @Override
    public void stop() {
        if (canal != null) {
            try {
                canal.close();
                canal.getConnection().close();
            } catch (IOException | TimeoutException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void enviarmessage(ActionEvent event) {
        event.consume();
        enviarmessage(message.getText().replace("\n", ""));
        message.setText("");
    }

    @FXML
    private void anhadirUsuario(ActionEvent event) {
        event.consume();
        setDestino(nouveauUtilisateur.getText());
        if (!listeMessages.containsKey(currentDestino)) {
            listeMessages.put(currentDestino, FXCollections.observableArrayList());
            listeAmis.add(currentDestino);
        }
        nouveauUtilisateur.setText(null);
    }

    public void enviarmessage(String broadcastMessage) {
        try (Connection conexion = new ConnectionFactory().newConnection();
                Channel canalEnvio = conexion.createChannel()) {
            canalEnvio.basicPublish("", currentDestino, null, (this.user + "##" + broadcastMessage).getBytes(StandardCharsets.UTF_8));
            System.out.println("Envoyer '" + broadcastMessage + "'");
            listeMessages.get(currentDestino).add("&!&" + broadcastMessage);
        } catch (IOException | TimeoutException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setDestino(String text) {
        this.currentDestino = text;
        liste.setDisable(false);
        message.setDisable(false);
        EnvoyerButton.setDisable(false);
        liste.setItems(listeMessages.get(text));
    }
}
