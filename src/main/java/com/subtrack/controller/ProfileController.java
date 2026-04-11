package com.subtrack.controller;

import com.subtrack.domain.Profile;
import com.subtrack.domain.User;
import com.subtrack.service.ProfileService;
import com.subtrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller para o diálogo de perfil e preferências.
 */
public class ProfileController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField alertDaysField;
    @FXML
    private TextField spendingLimitField;
    @FXML
    private Label feedbackLabel;

    private final ProfileService profileService = new ProfileService();
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            nameLabel.setText(user.getName());
            emailLabel.setText(user.getEmail());
        }

        Profile profile = profileService.getProfile(SessionManager.getCurrentUserId());
        alertDaysField.setText(String.valueOf(profile.getAlertDaysBefore()));
        spendingLimitField.setText(String.format("%.2f", profile.getMonthlySpendingLimit()));
    }

    @FXML
    private void handleSave() {
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().removeAll("label-success", "label-error");

        int alertDays;
        double spendingLimit;
        try {
            alertDays = Integer.parseInt(alertDaysField.getText().trim());
        } catch (NumberFormatException e) {
            feedbackLabel.getStyleClass().add("label-error");
            feedbackLabel.setText("Os dias de alerta devem ser um número inteiro válido.");
            return;
        }
        try {
            spendingLimit = Double.parseDouble(spendingLimitField.getText().trim());
        } catch (NumberFormatException e) {
            feedbackLabel.getStyleClass().add("label-error");
            feedbackLabel.setText("O limite de gastos deve ser um número válido.");
            return;
        }

        String error = profileService.updateProfile(SessionManager.getCurrentUserId(), alertDays, spendingLimit);
        if (error != null) {
            feedbackLabel.getStyleClass().add("label-error");
            feedbackLabel.setText(error);
        } else {
            feedbackLabel.getStyleClass().add("label-success");
            feedbackLabel.setText("Preferências salvas com sucesso!");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
