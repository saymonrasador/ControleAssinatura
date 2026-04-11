package com.subtrack.controller;

import com.subtrack.domain.*;
import com.subtrack.service.CategoryService;
import com.subtrack.service.PaymentMethodService;
import com.subtrack.service.SubscriptionService;
import com.subtrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para o diálogo de criação/edição de assinatura.
 */
public class SubscriptionDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<Periodicity> periodicityCombo;
    @FXML
    private DatePicker purchaseDatePicker;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private ComboBox<PaymentMethod> paymentMethodCombo;
    @FXML
    private CheckBox autoRenewCheck;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveBtn;

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final CategoryService categoryService = new CategoryService();
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();

    private Stage dialogStage;
    private Subscription editingSubscription;

    @FXML
    public void initialize() {
        String userId = SessionManager.getCurrentUserId();

        periodicityCombo.getItems().addAll(Periodicity.values());
        periodicityCombo.setValue(Periodicity.MENSA);

        List<Category> categories = categoryService.getAllByUserId(userId);
        categoryCombo.getItems().addAll(categories);
        if (!categories.isEmpty())
            categoryCombo.setValue(categories.get(0));

        List<PaymentMethod> methods = paymentMethodService.getAllByUserId(userId);
        paymentMethodCombo.getItems().addAll(methods);
        if (!methods.isEmpty())
            paymentMethodCombo.setValue(methods.get(0));

        purchaseDatePicker.setValue(LocalDate.now());
    }

    /**
     * Se estiver editando, preenche os campos do formulário com os dados da
     * assinatura existente.
     */
    public void setSubscription(Subscription sub) {
        this.editingSubscription = sub;
        if (sub != null) {
            titleLabel.setText("Editar Assinatura");
            saveBtn.setText("Atualizar");
            nameField.setText(sub.getName());
            priceField.setText(String.valueOf(sub.getPrice()));
            periodicityCombo.setValue(sub.getPeriodicity());
            purchaseDatePicker.setValue(sub.getPurchaseDate());
            autoRenewCheck.setSelected(sub.isAutoRenew());

            // Seleciona a categoria correspondente
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId().equals(sub.getCategoryId()))
                    .findFirst().ifPresent(categoryCombo::setValue);

            // Seleciona o método de pagamento correspondente
            paymentMethodCombo.getItems().stream()
                    .filter(m -> m.getId().equals(sub.getPaymentMethodId()))
                    .findFirst().ifPresent(paymentMethodCombo::setValue);
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String name = nameField.getText();
        String priceText = priceField.getText();
        Periodicity periodicity = periodicityCombo.getValue();
        LocalDate purchaseDate = purchaseDatePicker.getValue();
        boolean autoRenew = autoRenewCheck.isSelected();
        Category category = categoryCombo.getValue();
        PaymentMethod paymentMethod = paymentMethodCombo.getValue();

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Price must be a valid number.");
            return;
        }

        String error;
        if (editingSubscription != null) {
            error = subscriptionService.update(
                    editingSubscription.getId(), name, price, periodicity,
                    purchaseDate, autoRenew,
                    category != null ? category.getId() : null,
                    paymentMethod != null ? paymentMethod.getId() : null);
        } else {
            error = subscriptionService.create(
                    SessionManager.getCurrentUserId(), name, price, periodicity,
                    purchaseDate, autoRenew,
                    category != null ? category.getId() : null,
                    paymentMethod != null ? paymentMethod.getId() : null);
        }

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
