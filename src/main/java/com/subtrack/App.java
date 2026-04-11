package com.subtrack;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação SubTrack.
 * Inicializa o banco de dados e lança a tela de login.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Inicializa a conexão com o banco de dados e o schema
        try {
            DatabaseConfig.getConnection();
        } catch (Exception e) {
            System.err.println("Falha ao inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        primaryStage.setTitle("SubTrack — Gerenciador de Assinaturas");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);

        NavigationManager.setPrimaryStage(primaryStage);
        NavigationManager.navigateTo("login.fxml");
    }

    @Override
    public void stop() {
        DatabaseConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
