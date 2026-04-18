package com.subtrack.controller;

import com.subtrack.domain.Notification;
import com.subtrack.domain.Subscription;
import com.subtrack.service.NotificationService;
import com.subtrack.service.SubscriptionService;
import com.subtrack.util.NavigationManager;
import com.subtrack.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller para o diálogo pop-up de notificações.
 */
public class NotificationController {

    @FXML
    private ListView<Notification> notificationList;

    private final NotificationService notificationService = new NotificationService();
    private final SubscriptionService subscriptionService = new SubscriptionService();
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
     * Célula de lista personalizada para exibir notificações com tags de status
     * coloridas e botão de pagamento para status pendente.
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
                setText(null);

                // Determine status type from title
                String statusText;
                String tagColor;
                String tagTextColor;
                boolean isPendente = false;

                if (item.getTitle().startsWith("Venceu")) {
                    statusText = "ATRASADO";
                    tagColor = "#ff6b6b";
                    tagTextColor = "#ffffff";
                } else if (item.getTitle().startsWith("Vence em breve")) {
                    statusText = "ALERTA";
                    tagColor = "#ffc107";
                    tagTextColor = "#1a1a1a";
                } else {
                    statusText = "PENDENTE";
                    tagColor = "#e0e0e0";
                    tagTextColor = "#1a1a1a";
                    isPendente = true;
                }

                // Status tag
                Label statusTag = new Label(statusText);
                statusTag.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; " +
                                "-fx-padding: 2 8; -fx-background-radius: 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;",
                        tagColor, tagTextColor));

                // Read indicator
                String prefix = item.isRead() ? "" : "● ";
                Label titleLabel = new Label(prefix + item.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a1a;");

                HBox topRow = new HBox(8, titleLabel, statusTag);
                topRow.setAlignment(Pos.CENTER_LEFT);

                Label messageLabel = new Label(item.getMessage());
                messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                messageLabel.setWrapText(true);

                Label dateLabel = new Label(item.getCreatedAt().format(FMT));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

                HBox bottomRow = new HBox(8, dateLabel);
                bottomRow.setAlignment(Pos.CENTER_LEFT);

                // Add pay button for pending notifications
                if (isPendente && item.getSubscriptionId() != null) {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    Button payBtn = new Button("💰 Pagar");
                    payBtn.getStyleClass().addAll("btn-small");
                    payBtn.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: #ffffff; " +
                            "-fx-background-radius: 6; -fx-cursor: hand;");
                    payBtn.setOnAction(e -> handlePayFromNotification(item));
                    bottomRow.getChildren().addAll(spacer, payBtn);
                }

                VBox cellBox = new VBox(4, topRow, messageLabel, bottomRow);
                cellBox.setPadding(new Insets(8));

                if (!item.isRead()) {
                    cellBox.setStyle("-fx-background-color: #eef2f7; -fx-background-radius: 8;");
                } else {
                    cellBox.setStyle("-fx-background-color: transparent;");
                }

                setGraphic(cellBox);
                setStyle("-fx-padding: 2;");

                // Mark as read on click
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

    private void handlePayFromNotification(Notification notification) {
        Optional<Subscription> subOpt = subscriptionService.getById(notification.getSubscriptionId());
        if (subOpt.isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = NavigationManager.loadFXML("pay-confirmation-dialog.fxml");
            Parent root = loader.load();
            PayConfirmationController ctrl = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle("Confirmar Pagamento");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/subtrack/styles/style.css").toExternalForm());
            dialog.setScene(scene);
            ctrl.setDialogStage(dialog);
            ctrl.setSubscription(subOpt.get());
            dialog.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
