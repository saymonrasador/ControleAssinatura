package com.subtrack.controller;

import com.subtrack.service.AuthService;
import com.subtrack.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller para a tela de registro.
 */
public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        errorLabel.getStyleClass().removeAll("label-success");
        errorLabel.getStyleClass().add("label-error");

        String error = authService.register(
                nameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText());

        if (error != null) {
            errorLabel.setText(error);
        } else {
            errorLabel.getStyleClass().removeAll("label-error");
            errorLabel.getStyleClass().add("label-success");
            errorLabel.setText("Account created! You can now sign in.");
            // Limpa os campos
            nameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        }
    }

    @FXML
    private void handleLoginLink() {
        NavigationManager.navigateTo("login.fxml");
    }
}
