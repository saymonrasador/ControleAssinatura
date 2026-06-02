package com.subtrack.controller;

import com.subtrack.domain.Notification;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para o popup de alertas exibido ao carregar o dashboard.
 * Mostra notificações recém-geradas para assinaturas com status
 * Alerta, Atrasado ou Pendente.
 */
public class AlertPopupController {

    @FXML
    private ListView<Notification> alertList;
    @FXML
    private Label alertCountLabel;

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Define as notificações a serem exibidas no popup.
     */
    public void setAlerts(List<Notification> alerts) {
        alertList.getItems().setAll(alerts);
        alertCountLabel.setText(alerts.size() + " alerta(s)");
    }

    @FXML
    public void initialize() {
        alertList.setCellFactory(lv -> new AlertCell());
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    /**
     * Célula personalizada para exibir alertas com tags de status coloridas.
     */
    private static class AlertCell extends ListCell<Notification> {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        protected void updateItem(Notification item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                setText(null);

                // Determina o tipo de status a partir do título
                String statusText;
                String tagColor;
                String tagTextColor;
                String iconEmoji;

                if (item.getTitle().startsWith("Venceu")) {
                    statusText = "ATRASADO";
                    tagColor = "#ff6b6b";
                    tagTextColor = "#ffffff";
                    iconEmoji = "🔴";
                } else if (item.getTitle().startsWith("Vence em breve")) {
                    statusText = "ALERTA";
                    tagColor = "#ffc107";
                    tagTextColor = "#1a1a1a";
                    iconEmoji = "🟡";
                } else {
                    statusText = "PENDENTE";
                    tagColor = "#4a90d9";
                    tagTextColor = "#ffffff";
                    iconEmoji = "🔵";
                }

                // Tag de status
                Label statusTag = new Label(statusText);
                statusTag.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; " +
                                "-fx-padding: 2 8; -fx-background-radius: 10; " +
                                "-fx-font-size: 11px; -fx-font-weight: bold;",
                        tagColor, tagTextColor));

                // Ícone + título
                Label titleLabel = new Label(iconEmoji + " " + item.getTitle());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a1a;");

                HBox topRow = new HBox(8, titleLabel, statusTag);
                topRow.setAlignment(Pos.CENTER_LEFT);

                // Mensagem
                Label messageLabel = new Label(item.getMessage());
                messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
                messageLabel.setWrapText(true);

                // Data
                Label dateLabel = new Label(item.getCreatedAt().format(FMT));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

                VBox cellBox = new VBox(4, topRow, messageLabel, dateLabel);
                cellBox.setPadding(new Insets(8));

                // Cor de fundo baseada no status
                String bgColor;
                if (item.getTitle().startsWith("Venceu")) {
                    bgColor = "#fff0f0";
                } else if (item.getTitle().startsWith("Vence em breve")) {
                    bgColor = "#fff8e1";
                } else {
                    bgColor = "#e8f0ff";
                }
                cellBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");

                setGraphic(cellBox);
                setStyle("-fx-padding: 2;");
            }
        }
    }
}
