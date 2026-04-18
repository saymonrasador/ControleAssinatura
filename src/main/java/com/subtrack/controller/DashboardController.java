package com.subtrack.controller;

import com.subtrack.domain.*;
import com.subtrack.service.*;
import com.subtrack.util.DateUtil;
import com.subtrack.util.NavigationManager;
import com.subtrack.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controle do dashboard principal gerenciando exibição de assinaturas,
 * gráficos, filtros e navegação.
 */
public class DashboardController {

    @FXML
    private Button notificationBtn;
    @FXML
    private Label spendingLabel;
    @FXML
    private Label limitLabel;
    @FXML
    private ProgressBar spendingProgress;
    @FXML
    private Label activeCountLabel;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> paymentMethodFilter;
    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<Subscription> subscriptionTable;
    @FXML
    private TableColumn<Subscription, String> colName;
    @FXML
    private TableColumn<Subscription, String> colPrice;
    @FXML
    private TableColumn<Subscription, String> colPeriod;
    @FXML
    private TableColumn<Subscription, String> colNextDue;
    @FXML
    private TableColumn<Subscription, String> colCategory;
    @FXML
    private TableColumn<Subscription, String> colPayMethod;
    @FXML
    private TableColumn<Subscription, String> colStatus;
    @FXML
    private TableColumn<Subscription, Void> colActions;

    @FXML
    private PieChart categoryPieChart;
    @FXML
    private PieChart paymentMethodPieChart;

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final DashboardService dashboardService = new DashboardService();
    private final ProfileService profileService = new ProfileService();
    private final NotificationService notificationService = new NotificationService();
    private final CategoryService categoryService = new CategoryService();
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private final PaymentService paymentService = new PaymentService();
    private final AuthService authService = new AuthService();

    private ObservableList<Subscription> allSubscriptions = FXCollections.observableArrayList();
    private FilteredList<Subscription> filteredSubscriptions;

    @FXML
    public void initialize() {
        String userId = SessionManager.getCurrentUserId();
        Profile profile = profileService.getProfile(userId);

        // Generate alerts on dashboard load
        notificationService.generateAlerts(userId, profile.getAlertDaysBefore());

        setupTableColumns();
        setupFilters();
        loadData();
        updateNotificationButton();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getPrice())));
        colPeriod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeriodicity().name()));
        colNextDue
                .setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDate(c.getValue().getNextDueDate())));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategoryName() != null ? c.getValue().getCategoryName() : "—"));
        colPayMethod.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPaymentMethodName() != null ? c.getValue().getPaymentMethodName() : "—"));

        // Coluna Status com estilo de cor
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PAGO" -> setStyle("-fx-text-fill: #4ecca3; -fx-font-weight: bold;");
                        case "ATRASADO" -> setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                        case "ALERTA" -> setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #ffc107;");
                    }
                }
            }
        });

        // Coluna Ações com botões Editar, Excluir, Pagar
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button deleteBtn = new Button("Excluir");
            private final Button payBtn = new Button("Pagar");
            private final HBox box = new HBox(4, editBtn, payBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("btn-secondary", "btn-small");
                deleteBtn.getStyleClass().addAll("btn-danger", "btn-small");
                payBtn.getStyleClass().addAll("btn-success", "btn-small");

                editBtn.setOnAction(e -> handleEditSubscription(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteSubscription(getTableView().getItems().get(getIndex())));
                payBtn.setOnAction(e -> handlePaySubscription(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Subscription sub = getTableView().getItems().get(getIndex());
                    boolean isPago = sub.getStatus() == SubscriptionStatus.PAGO;
                    payBtn.setDisable(isPago);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupFilters() {
        String userId = SessionManager.getCurrentUserId();

        // Preenche os filtros
        List<Category> categories = categoryService.getAllByUserId(userId);
        categoryFilter.getItems().add("Todas as Categorias");
        categories.forEach(c -> categoryFilter.getItems().add(c.getName()));

        List<PaymentMethod> methods = paymentMethodService.getAllByUserId(userId);
        paymentMethodFilter.getItems().add("Todos os Métodos");
        methods.forEach(m -> paymentMethodFilter.getItems().add(m.getName()));

        statusFilter.getItems().addAll("Todos os Status", "PENDENTE", "ALERTA", "ATRASADO", "PAGO");

        // Adiciona listeners aos filtros
        searchField.textProperty().addListener((o, ov, nv) -> applyFilters());
        categoryFilter.setOnAction(e -> applyFilters());
        paymentMethodFilter.setOnAction(e -> applyFilters());
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void loadData() {
        String userId = SessionManager.getCurrentUserId();
        Profile profile = profileService.getProfile(userId);
        int alertDays = profile.getAlertDaysBefore();

        List<Subscription> subs = subscriptionService.getActiveSubscriptions(userId, alertDays);
        allSubscriptions.setAll(subs);
        filteredSubscriptions = new FilteredList<>(allSubscriptions, p -> true);
        subscriptionTable.setItems(filteredSubscriptions);

        updateSummary(subs, profile);
        updateCharts(subs);
    }

    private void applyFilters() {
        filteredSubscriptions.setPredicate(sub -> {
            // Filtro de pesquisa
            String search = searchField.getText();
            if (search != null && !search.isEmpty()) {
                if (!sub.getName().toLowerCase().contains(search.toLowerCase()))
                    return false;
            }

            // Filtro de categoria
            String cat = categoryFilter.getValue();
            if (cat != null && !cat.equals("Todas as Categorias")) {
                if (sub.getCategoryName() == null || !sub.getCategoryName().equals(cat))
                    return false;
            }

            // Filtro de método de pagamento
            String pm = paymentMethodFilter.getValue();
            if (pm != null && !pm.equals("Todos os Métodos")) {
                if (sub.getPaymentMethodName() == null || !sub.getPaymentMethodName().equals(pm))
                    return false;
            }

            // Filtro de status
            String status = statusFilter.getValue();
            if (status != null && !status.equals("Todos os Status")) {
                if (!sub.getStatus().name().equals(status))
                    return false;
            }

            return true;
        });

        // Atualiza os gráficos com base nos dados filtrados
        updateCharts(filteredSubscriptions);
    }

    private void updateSummary(List<Subscription> subs, Profile profile) {
        double monthlySpending = dashboardService.computeMonthlySpending(subs);
        spendingLabel.setText(String.format("R$%.2f", monthlySpending));
        activeCountLabel.setText(String.valueOf(subs.size()));

        double limit = profile.getMonthlySpendingLimit();
        if (limit > 0) {
            limitLabel.setText(String.format("de R$%.2f limite", limit));
            double ratio = Math.min(monthlySpending / limit, 1.0);
            spendingProgress.setProgress(ratio);
            // Muda a cor da barra com base na proporção
            if (ratio > 0.9) {
                spendingProgress.setStyle("-fx-accent: #ff6b6b;");
            } else if (ratio > 0.7) {
                spendingProgress.setStyle("-fx-accent: #ffc107;");
            } else {
                spendingProgress.setStyle("-fx-accent: #4ecca3;");
            }
        } else {
            limitLabel.setText("Sem limite definido");
            spendingProgress.setProgress(0);
        }
    }

    private void updateCharts(List<? extends Subscription> subs) {
        // Gráfico de pizza por categoria
        Map<String, Double> categoryData = dashboardService.getSpendingByCategory(
                subs.stream().map(s -> (Subscription) s).toList());
        categoryPieChart.getData().clear();
        categoryData.forEach((name, amount) -> categoryPieChart.getData().add(new PieChart.Data(
                String.format("%s (R$%.0f)", name, amount), amount)));

        // Gráfico de pizza por método de pagamento
        Map<String, Double> pmData = dashboardService.getSpendingByPaymentMethod(
                subs.stream().map(s -> (Subscription) s).toList());
        paymentMethodPieChart.getData().clear();
        pmData.forEach((name, amount) -> paymentMethodPieChart.getData().add(new PieChart.Data(
                String.format("%s (R$%.0f)", name, amount), amount)));
    }

    private void updateNotificationButton() {
        int unread = notificationService.getUnreadCount(SessionManager.getCurrentUserId());
        notificationBtn.setText(unread > 0 ? "🔔 Alertas (" + unread + ")" : "🔔 Alertas");
    }

    // --- Handlers de Ação ---

    @FXML
    private void handleAddSubscription() {
        openSubscriptionDialog(null);
    }

    private void handleEditSubscription(Subscription sub) {
        openSubscriptionDialog(sub);
    }

    private void handleDeleteSubscription(Subscription sub) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Desativar assinatura '" + sub.getName() + "'?\nO histórico de pagamentos será preservado.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar Desativação");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                subscriptionService.delete(sub.getId());
                loadData();
            }
        });
    }

    private void handlePaySubscription(Subscription sub) {
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
            ctrl.setSubscription(sub);
            dialog.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível abrir o diálogo de pagamento.", Alert.AlertType.ERROR);
        }
    }

    private void openSubscriptionDialog(Subscription existingSub) {
        try {
            FXMLLoader loader = NavigationManager.loadFXML("subscription-dialog.fxml");
            Parent root = loader.load();
            SubscriptionDialogController ctrl = loader.getController();
            ctrl.setSubscription(existingSub);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle(existingSub == null ? "Nova Assinatura" : "Editar Assinatura");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/subtrack/styles/style.css").toExternalForm());
            dialog.setScene(scene);
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategories() {
        openManagerDialog("category-manager.fxml", "Manage Categories");
    }

    @FXML
    private void handlePaymentMethods() {
        openManagerDialog("payment-method-manager.fxml", "Manage Payment Methods");
    }

    private void openManagerDialog(String fxml, String title) {
        try {
            FXMLLoader loader = NavigationManager.loadFXML(fxml);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle(title);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/subtrack/styles/style.css").toExternalForm());
            dialog.setScene(scene);

            // Define o manipulador de fechamento
            if (loader.getController() instanceof CategoryManagerController ctrl) {
                ctrl.setDialogStage(dialog);
            } else if (loader.getController() instanceof PaymentMethodManagerController ctrl) {
                ctrl.setDialogStage(dialog);
            }

            dialog.showAndWait();
            // Atualiza os filtros e os dados após o fechamento do gerenciador
            refreshFilters();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePaymentHistory() {
        NavigationManager.navigateTo("payment-history.fxml");
    }

    @FXML
    private void handleNotifications() {
        try {
            FXMLLoader loader = NavigationManager.loadFXML("notifications.fxml");
            Parent root = loader.load();
            NotificationController ctrl = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle("Notifications");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/subtrack/styles/style.css").toExternalForm());
            dialog.setScene(scene);
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();
            updateNotificationButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = NavigationManager.loadFXML("profile.fxml");
            Parent root = loader.load();
            ProfileController ctrl = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setTitle("Profile");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/subtrack/styles/style.css").toExternalForm());
            dialog.setScene(scene);
            ctrl.setDialogStage(dialog);
            dialog.showAndWait();
            loadData(); // Recarrega para refletir as preferências alteradas
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        NavigationManager.navigateTo("login.fxml");
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        paymentMethodFilter.setValue(null);
        statusFilter.setValue(null);
        applyFilters();
    }

    private void refreshFilters() {
        String userId = SessionManager.getCurrentUserId();

        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("Todas as Categorias");
        categoryService.getAllByUserId(userId).forEach(c -> categoryFilter.getItems().add(c.getName()));

        paymentMethodFilter.getItems().clear();
        paymentMethodFilter.getItems().add("Todos os Métodos");
        paymentMethodService.getAllByUserId(userId).forEach(m -> paymentMethodFilter.getItems().add(m.getName()));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
