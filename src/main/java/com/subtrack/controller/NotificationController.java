package com.subtrack.controller;

import com.subtrack.domain.Notification;
import com.subtrack.service.NotificationService;
import com.subtrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para o diálogo pop-up de notificações.
 */
public class NotificationController {

    @FXML
    private ListView<Notification> notificationList;

    private final NotificationService notificationService = new NotificationService();
    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        notificationList.setCellFactory(lv -> new NotificationCell());
        loadData();
    }

    private void loadData() {
        String userId = SessionManager.getCurrentUserId();
        List<Notification> notifications = notificationService.getAll(userId);
        notificationList.getItems().setAll(notifications);
    }

    @FXML
    private void handleMarkAll() {
        notificationService.markAllAsRead(SessionManager.getCurrentUserId());
        loadData();
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    /**
     * Célula de lista personalizada para exibir notificações com estado visual de
     * lido/não lido.
     */
    private class NotificationCell extends ListCell<Notification> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        @Override
        protected void updateItem(Notification item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                String prefix = item.isRead() ? "  " : "● ";
                setText(prefix + item.getTitle() + "\n   " +
                        item.getMessage() + "\n   " +
                        item.getCreatedAt().format(FMT));

                if (!item.isRead()) {
                    setStyle("-fx-background-color: #1e2a45; -fx-text-fill: #e8e8e8; " +
                            "-fx-font-size: 12px; -fx-padding: 8;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #6c6c8a; " +
                            "-fx-font-size: 12px; -fx-padding: 8;");
                }

                // Marca como lido ao clicar
                setOnMouseClicked(e -> {
                    if (!item.isRead()) {
                        notificationService.markAsRead(item.getId());
                        item.setRead(true);
                        updateItem(item, false);
                    }
                });
            }
        }
    }
}
