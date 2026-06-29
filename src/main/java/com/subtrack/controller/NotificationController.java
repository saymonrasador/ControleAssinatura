package com.subtrack.controller;

import com.subtrack.domain.Notification;
import com.subtrack.domain.Subscription;
import com.subtrack.domain.SubscriptionStatus;
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
        // Marca todas como lidas ao abrir as notificações (limpa o badge de não lidas).
        // A lista exibida mantém o destaque de "não lido" desta visualização; o banco
        // já fica atualizado para a próxima abertura.
        notificationService.markAllAsRead(SessionManager.getCurrentUserId());
    }

    private void loadData() {
        String userId = SessionManager.getCurrentUserId();
        List<Notification> notifications = notificationService.getAll(userId);
        notificationList.getItems().setAll(notifications);
    }

    @FXML
    private void handleDeleteAll() {
        if (notificationList.getItems().isEmpty()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir todas as notificações?\nEsta ação não pode ser desfeita.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                notificationService.deleteAll(SessionManager.getCurrentUserId());
                loadData();
            }
        });
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
                boolean isAtrasado = false;

                boolean read = item.isRead();

                if (item.getTitle().startsWith("Venceu")) {
                    statusText = "ATRASADO";
                    tagColor = read ? "#e8c8c8" : "#ff6b6b";
                    tagTextColor = read ? "#aaaaaa" : "#ffffff";
                    isAtrasado = true;
                } else if (item.getTitle().startsWith("Vence em breve")) {
                    statusText = "ALERTA";
                    tagColor = read ? "#e8dfc0" : "#ff9800";
                    tagTextColor = read ? "#aaaaaa" : "#ffffff";
                } else {
                    statusText = "PENDENTE";
                    tagColor = read ? "#d0d0d0" : "#5b9cf6";
                    tagTextColor = read ? "#aaaaaa" : "#ffffff";
                    isPendente = true;
                }

                // Status tag
                Label statusTag = new Label(statusText);
                statusTag.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; " +
                                "-fx-padding: 2 8; -fx-background-radius: 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;",
                        tagColor, tagTextColor));

                // Title — bold/dark when unread, muted when read
                String prefix = read ? "" : "● ";
                Label titleLabel = new Label(prefix + item.getTitle());
                titleLabel.setStyle(String.format(
                        "-fx-font-weight: %s; -fx-font-size: 13px; -fx-text-fill: %s;",
                        read ? "normal" : "bold",
                        read ? "#b0b0b0" : "#1a1a1a"));

                HBox topRow = new HBox(8, titleLabel, statusTag);
                topRow.setAlignment(Pos.CENTER_LEFT);

                Label messageLabel = new Label(item.getMessage());
                messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (read ? "#c0c0c0" : "#555555") + ";");
                messageLabel.setWrapText(true);

                Label dateLabel = new Label(item.getCreatedAt().format(FMT));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (read ? "#cccccc" : "#888888") + ";");

                HBox bottomRow = new HBox(8, dateLabel);
                bottomRow.setAlignment(Pos.CENTER_LEFT);

                // Add pay button for pending and overdue notifications
                if ((isPendente || isAtrasado) && item.getSubscriptionId() != null) {
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Verifica se a assinatura já está paga neste ciclo
                    Optional<Subscription> subOpt = subscriptionService.getById(item.getSubscriptionId());
                    boolean isPago = subOpt.isPresent()
                            && subOpt.get().getStatus() == SubscriptionStatus.PAGO;

                    Button payBtn;
                    if (isPago) {
                        payBtn = new Button("✅ Pago");
                        payBtn.getStyleClass().addAll("btn-small");
                        payBtn.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; " +
                                "-fx-background-radius: 6;");
                        payBtn.setDisable(true);
                    } else {
                        payBtn = new Button("💰 Pagar");
                        payBtn.getStyleClass().addAll("btn-small");
                        payBtn.setStyle("-fx-background-color: #4ecca3; -fx-text-fill: #ffffff; " +
                                "-fx-background-radius: 6; -fx-cursor: hand;");
                        payBtn.setOnAction(e -> handlePayFromNotification(item));
                    }
                    bottomRow.getChildren().addAll(spacer, payBtn);
                }

                VBox cellBox = new VBox(6, topRow, messageLabel, bottomRow);
                cellBox.setPadding(new Insets(10, 12, 10, 12));

                if (!item.isRead()) {
                    cellBox.setStyle("-fx-background-color: #eef2f7; -fx-background-radius: 8; " +
                            "-fx-border-color: #c8d8f0; -fx-border-radius: 8; -fx-border-width: 1;");
                } else {
                    cellBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6;");
                }

                setGraphic(cellBox);
                setStyle("-fx-padding: 4 6; -fx-background-color: transparent;");
                setOnMouseClicked(null);
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
