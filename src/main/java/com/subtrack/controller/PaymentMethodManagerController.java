package com.subtrack.controller;

import com.subtrack.domain.PaymentMethod;
import com.subtrack.service.PaymentMethodService;
import com.subtrack.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller para o diálogo de gerenciamento de métodos de pagamento.
 */
public class PaymentMethodManagerController {

    @FXML
    private TextField nameField;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button saveBtn;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<PaymentMethod> methodTable;
    @FXML
    private TableColumn<PaymentMethod, String> colColor;
    @FXML
    private TableColumn<PaymentMethod, String> colName;
    @FXML
    private TableColumn<PaymentMethod, String> colDefault;
    @FXML
    private TableColumn<PaymentMethod, Void> colActions;

    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private Stage dialogStage;
    private PaymentMethod editingMethod;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        colColor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PaymentMethod pm = getTableView().getItems().get(getIndex());
                    Rectangle rect = new Rectangle(16, 16);
                    try {
                        rect.setFill(pm.getColorHex() != null
                                ? Color.web(pm.getColorHex())
                                : Color.GRAY);
                    } catch (Exception e) {
                        rect.setFill(Color.GRAY);
                    }
                    rect.setArcWidth(4);
                    rect.setArcHeight(4);
                    setGraphic(rect);
                }
            }
        });
        colColor.setCellValueFactory(c -> new SimpleStringProperty(""));

        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colDefault.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDefault() ? "✓" : ""));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button deleteBtn = new Button("Excluir");
            private final HBox box = new HBox(4, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");

                editBtn.setOnAction(e -> {
                    PaymentMethod pm = getTableView().getItems().get(getIndex());
                    startEditing(pm);
                });
                deleteBtn.setOnAction(e -> {
                    PaymentMethod pm = getTableView().getItems().get(getIndex());
                    handleDelete(pm);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PaymentMethod pm = getTableView().getItems().get(getIndex());
                    deleteBtn.setDisable(pm.isDefault());
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        String userId = SessionManager.getCurrentUserId();
        List<PaymentMethod> methods = paymentMethodService.getAllByUserId(userId);
        methodTable.setItems(FXCollections.observableArrayList(methods));
    }

    private void startEditing(PaymentMethod pm) {
        editingMethod = pm;
        nameField.setText(pm.getName());
        try {
            colorPicker.setValue(pm.getColorHex() != null ? Color.web(pm.getColorHex()) : Color.GRAY);
        } catch (Exception e) {
            colorPicker.setValue(Color.GRAY);
        }
        saveBtn.setText("Atualizar");
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");
        String userId = SessionManager.getCurrentUserId();
        String name = nameField.getText();
        String color = String.format("#%02X%02X%02X",
                (int) (colorPicker.getValue().getRed() * 255),
                (int) (colorPicker.getValue().getGreen() * 255),
                (int) (colorPicker.getValue().getBlue() * 255));

        String error;
        if (editingMethod != null) {
            error = paymentMethodService.update(editingMethod.getId(), userId, name, color);
        } else {
            error = paymentMethodService.create(userId, name, color);
        }

        if (error != null) {
            errorLabel.setText(error);
        } else {
            clearForm();
            loadData();
        }
    }

    private void handleDelete(PaymentMethod pm) {
        String userId = SessionManager.getCurrentUserId();
        String error = paymentMethodService.delete(pm.getId(), userId);
        if (error != null) {
            errorLabel.setText(error);
        } else {
            loadData();
        }
    }

    private void clearForm() {
        nameField.clear();
        colorPicker.setValue(Color.WHITE);
        editingMethod = null;
        saveBtn.setText("Adicionar");
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}
