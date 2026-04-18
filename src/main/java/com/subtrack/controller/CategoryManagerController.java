package com.subtrack.controller;

import com.subtrack.domain.Category;
import com.subtrack.service.CategoryService;
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
 * Controller para o diálogo de gerenciamento de categorias.
 */
public class CategoryManagerController {

    @FXML
    private TextField nameField;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button saveBtn;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> colColor;
    @FXML
    private TableColumn<Category, String> colName;
    @FXML
    private TableColumn<Category, String> colDefault;
    @FXML
    private TableColumn<Category, Void> colActions;

    private final CategoryService categoryService = new CategoryService();
    private Stage dialogStage;
    private Category editingCategory;

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
                    Category cat = getTableView().getItems().get(getIndex());
                    Rectangle rect = new Rectangle(16, 16);
                    try {
                        rect.setFill(cat.getColorHex() != null
                                ? Color.web(cat.getColorHex())
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
            private final Button deleteBtn = new Button("Del");
            private final HBox box = new HBox(4, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");

                editBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    startEditing(cat);
                });
                deleteBtn.setOnAction(e -> {
                    Category cat = getTableView().getItems().get(getIndex());
                    handleDelete(cat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Category cat = getTableView().getItems().get(getIndex());
                    deleteBtn.setDisable(cat.isDefault());
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        String userId = SessionManager.getCurrentUserId();
        List<Category> categories = categoryService.getAllByUserId(userId);
        categoryTable.setItems(FXCollections.observableArrayList(categories));
    }

    private void startEditing(Category cat) {
        editingCategory = cat;
        nameField.setText(cat.getName());
        try {
            colorPicker.setValue(cat.getColorHex() != null ? Color.web(cat.getColorHex()) : Color.GRAY);
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
        if (editingCategory != null) {
            error = categoryService.update(editingCategory.getId(), userId, name, color);
        } else {
            error = categoryService.create(userId, name, color);
        }

        if (error != null) {
            errorLabel.setText(error);
        } else {
            clearForm();
            loadData();
        }
    }

    private void handleDelete(Category cat) {
        String userId = SessionManager.getCurrentUserId();
        String error = categoryService.delete(cat.getId(), userId);
        if (error != null) {
            errorLabel.setText(error);
        } else {
            loadData();
        }
    }

    private void clearForm() {
        nameField.clear();
        colorPicker.setValue(Color.WHITE);
        editingCategory = null;
        saveBtn.setText("Adicionar");
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}
