package com.subtrack.controller;

import com.subtrack.service.AuthService;
import com.subtrack.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller para a tela de login.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        String error = authService.login(emailField.getText(), passwordField.getText());
        if (error != null) {
            errorLabel.setText(error);
        } else {
            NavigationManager.navigateTo("dashboard.fxml");
        }
    }

    @FXML
    private void handleRegisterLink() {
        NavigationManager.navigateTo("register.fxml");
    }
}
