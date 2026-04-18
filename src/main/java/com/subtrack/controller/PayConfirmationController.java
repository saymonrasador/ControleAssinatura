package com.subtrack.controller;

import com.subtrack.domain.Subscription;
import com.subtrack.service.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controlador do popup de confirmação de pagamento.
 * Permite editar o valor e a data antes de salvar no histórico.
 */
public class PayConfirmationController {

    @FXML
    private Label subscriptionNameLabel;
    @FXML
    private TextField amountField;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private Subscription subscription;
    private final PaymentService paymentService = new PaymentService();

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Define a assinatura e pré-preenche os campos com valor e data padrão.
     */
    public void setSubscription(Subscription sub) {
        this.subscription = sub;
        subscriptionNameLabel.setText("Assinatura: " + sub.getName());
        amountField.setText(String.format("%.2f", sub.getPrice()).replace('.', ','));
        paymentDatePicker.setValue(LocalDate.now());
        errorLabel.setText("");
    }

    @FXML
    private void handleConfirm() {
        errorLabel.setText("");

        // Valida valor
        double amount;
        try {
            String raw = amountField.getText().trim().replace(',', '.');
            amount = Double.parseDouble(raw);
            if (amount <= 0) {
                errorLabel.setText("O valor deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Valor inválido. Use o formato: 29,90");
            return;
        }

        // Valida data
        LocalDate paymentDate = paymentDatePicker.getValue();
        if (paymentDate == null) {
            errorLabel.setText("Selecione uma data de pagamento.");
            return;
        }

        // Registra o pagamento com valor e data customizados
        String error = paymentService.registerPayment(subscription.getId(), amount, paymentDate);
        if (error != null) {
            errorLabel.setText(error);
        } else {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
