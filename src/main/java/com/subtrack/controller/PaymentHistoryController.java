package com.subtrack.controller;

import com.subtrack.domain.PaymentRecord;
import com.subtrack.domain.Subscription;
import com.subtrack.service.PaymentService;
import com.subtrack.service.SubscriptionService;
import com.subtrack.service.ProfileService;
import com.subtrack.util.DateUtil;
import com.subtrack.util.NavigationManager;
import com.subtrack.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para a tela de histórico de pagamentos.
 */
public class PaymentHistoryController {

    @FXML
    private ComboBox<String> subscriptionFilter;
    @FXML
    private TextField categoryFilterField;
    @FXML
    private TextField payMethodFilterField;
    @FXML
    private TextField competenceField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<PaymentRecord> historyTable;
    @FXML
    private TableColumn<PaymentRecord, String> colSubscription;
    @FXML
    private TableColumn<PaymentRecord, String> colCategory;
    @FXML
    private TableColumn<PaymentRecord, String> colPayMethod;
    @FXML
    private TableColumn<PaymentRecord, String> colAmount;
    @FXML
    private TableColumn<PaymentRecord, String> colDate;
    @FXML
    private TableColumn<PaymentRecord, String> colCompetence;

    @FXML
    private PieChart historyPieChart;
    @FXML
    private Label totalLabel;

    private final PaymentService paymentService = new PaymentService();
    private final SubscriptionService subscriptionService = new SubscriptionService();
    private final ProfileService profileService = new ProfileService();

    // Mapeia nomes de assinaturas para IDs para filtragem
    private final Map<String, String> subscriptionNameToId = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadData(null, null, null, null, null, null);
    }

    private void setupTableColumns() {
        colSubscription.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubscriptionNameSnapshot()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoryNameSnapshot()));
        colPayMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethodNameSnapshot()));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(String.format("R$%.2f", c.getValue().getAmount())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDate(c.getValue().getPaymentDate())));
        colCompetence.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompetence()));
    }

    private void setupFilters() {
        String userId = SessionManager.getCurrentUserId();
        int alertDays = profileService.getProfile(userId).getAlertDaysBefore();

        subscriptionFilter.getItems().add("Todas as Assinaturas");
        List<Subscription> subs = subscriptionService.getAllSubscriptions(userId, alertDays);
        for (Subscription sub : subs) {
            subscriptionFilter.getItems().add(sub.getName());
            subscriptionNameToId.put(sub.getName(), sub.getId());
        }
    }

    private void loadData(String subscriptionId, String categoryFilter,
            String payMethodFilter, String competence,
            LocalDate startDate, LocalDate endDate) {
        String userId = SessionManager.getCurrentUserId();
        List<PaymentRecord> records = paymentService.getFilteredHistory(
                userId, subscriptionId, categoryFilter, payMethodFilter,
                competence, startDate, endDate);

        historyTable.setItems(FXCollections.observableArrayList(records));
        updateChart(records);
        updateTotal(records);
    }

    private void updateChart(List<PaymentRecord> records) {
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (PaymentRecord r : records) {
            String cat = r.getCategoryNameSnapshot() != null ? r.getCategoryNameSnapshot() : "Desconhecido";
            categoryTotals.merge(cat, r.getAmount(), Double::sum);
        }
        historyPieChart.getData().clear();
        categoryTotals.forEach((name, amount) -> historyPieChart.getData().add(new PieChart.Data(
                String.format("%s (R$%.0f)", name, amount), amount)));
    }

    private void updateTotal(List<PaymentRecord> records) {
        double total = records.stream().mapToDouble(PaymentRecord::getAmount).sum();
        totalLabel.setText(String.format("Total: R$%.2f", total));
    }

    @FXML
    private void handleSearch() {
        String subName = subscriptionFilter.getValue();
        String subId = null;
        if (subName != null && !subName.equals("Todas as Assinaturas")) {
            subId = subscriptionNameToId.get(subName);
        }

        String catFilter = categoryFilterField.getText();
        String pmFilter = payMethodFilterField.getText();
        String competence = competenceField.getText();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        loadData(subId,
                catFilter.isEmpty() ? null : catFilter,
                pmFilter.isEmpty() ? null : pmFilter,
                competence.isEmpty() ? null : competence,
                start, end);
    }

    @FXML
    private void handleClear() {
        subscriptionFilter.setValue(null);
        categoryFilterField.clear();
        payMethodFilterField.clear();
        competenceField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadData(null, null, null, null, null, null);
    }

    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("dashboard.fxml");
    }
}
