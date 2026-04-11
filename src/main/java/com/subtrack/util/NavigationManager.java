package com.subtrack.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gerencia a navegação entre cenas trocando cenas no palco principal.
 */
public class NavigationManager {

    private static Stage primaryStage;

    private NavigationManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navega para uma nova visualização carregando o arquivo FXML especificado.
     * Os arquivos FXML estão localizados em /com/subtrack/views/.
     *
     * @param fxmlFile nome do arquivo (ex: "login.fxml")
     */
    public static void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource("/com/subtrack/views/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    NavigationManager.class.getResource("/com/subtrack/styles/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar visualização: " + fxmlFile, e);
        }
    }

    /**
     * Carrega um arquivo FXML e retorna o FXMLLoader para acessar o controlador.
     */
    public static FXMLLoader loadFXML(String fxmlFile) {
        return new FXMLLoader(
                NavigationManager.class.getResource("/com/subtrack/views/" + fxmlFile));
    }
}
