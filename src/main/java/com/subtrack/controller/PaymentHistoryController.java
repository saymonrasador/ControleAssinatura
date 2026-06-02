package com.subtrack.controller;

import com.subtrack.domain.PaymentRecord;
import com.subtrack.service.PaymentService;
import com.subtrack.util.DateUtil;
import com.subtrack.util.NavigationManager;
import com.subtrack.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Controller para a tela de histórico de pagamentos.
 */
public class PaymentHistoryController {

    // ── Filtros ──────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> subscriptionFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> payMethodFilter;
    @FXML private TextField        competenceField;
    @FXML private DatePicker       startDatePicker;
    @FXML private DatePicker       endDatePicker;

    // ── Tabela ───────────────────────────────────────────────────────────────
    @FXML private TableView<PaymentRecord>           historyTable;
    @FXML private TableColumn<PaymentRecord, String> colSubscription;
    @FXML private TableColumn<PaymentRecord, String> colCategory;
    @FXML private TableColumn<PaymentRecord, String> colPayMethod;
    @FXML private TableColumn<PaymentRecord, String> colAmount;
    @FXML private TableColumn<PaymentRecord, String> colDate;
    @FXML private TableColumn<PaymentRecord, String> colCompetence;

    // ── Gráfico de pizza ─────────────────────────────────────────────────────
    @FXML private ComboBox<String> chartModeSelector;
    @FXML private PieChart         historyPieChart;
    @FXML private Label            totalLabel;

    // ── Gráfico de linha (tendência) ──────────────────────────────────────────
    @FXML private LineChart<String, Number>  trendChart;
    @FXML private CategoryAxis               trendXAxis;
    @FXML private NumberAxis                 trendYAxis;

    // ── Constantes de modo do gráfico de pizza ────────────────────────────────
    private static final String MODE_CATEGORY   = "Categoria";
    private static final String MODE_PAY_METHOD = "Método de Pagamento";

    /** Número de meses exibidos por padrão no gráfico de tendência */
    private static final int DEFAULT_MONTHS = 12;

    private final PaymentService paymentService = new PaymentService();

    /** Mapeia nomes de assinaturas → IDs para filtragem */
    private final Map<String, String> subscriptionNameToId = new LinkedHashMap<>();

    /** Registros atualmente filtrados */
    private List<PaymentRecord> currentRecords = List.of();

    // ── Inicialização ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupChartModeSelector();
        loadData(null, null, null, null, null, null);
    }

    private void setupTableColumns() {
        colSubscription.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubscriptionNameSnapshot()));
        colCategory    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoryNameSnapshot()));
        colPayMethod   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethodNameSnapshot()));
        colAmount      .setCellValueFactory(c -> new SimpleStringProperty(String.format("R$%.2f", c.getValue().getAmount())));
        colDate        .setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDate(c.getValue().getPaymentDate())));
        colCompetence  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompetence()));
    }

    private void setupFilters() {
        String userId = SessionManager.getCurrentUserId();

        subscriptionFilter.getItems().add("Todas as Assinaturas");
        Map<String, String> histSubs = paymentService.getDistinctSubscriptions(userId);
        histSubs.forEach((name, id) -> {
            subscriptionFilter.getItems().add(name);
            subscriptionNameToId.put(name, id);
        });

        categoryFilter.getItems().add("Todas as Categorias");
        categoryFilter.getItems().addAll(paymentService.getDistinctCategories(userId));

        payMethodFilter.getItems().add("Todos os Métodos");
        payMethodFilter.getItems().addAll(paymentService.getDistinctPaymentMethods(userId));
    }

    private void setupChartModeSelector() {
        chartModeSelector.getItems().addAll(MODE_CATEGORY, MODE_PAY_METHOD);
        chartModeSelector.setValue(MODE_CATEGORY);
    }

    // ── Carga de dados ───────────────────────────────────────────────────────

    private void loadData(String subscriptionId, String categoryFilter,
                          String payMethodFilter, String competence,
                          LocalDate startDate, LocalDate endDate) {
        String userId = SessionManager.getCurrentUserId();
        currentRecords = paymentService.getFilteredHistory(
                userId, subscriptionId, categoryFilter, payMethodFilter,
                competence, startDate, endDate);

        historyTable.setItems(FXCollections.observableArrayList(currentRecords));
        updatePieChart(currentRecords);
        updateTotal(currentRecords);
        updateTrendChart(currentRecords, startDate, endDate, competence);
    }

    // ── Gráfico de pizza ─────────────────────────────────────────────────────

    private void updatePieChart(List<PaymentRecord> records) {
        String mode = chartModeSelector.getValue();
        if (MODE_PAY_METHOD.equals(mode)) {
            buildPieChart("Gastos por Método de Pagamento", records,
                    r -> r.getPaymentMethodNameSnapshot());
        } else {
            buildPieChart("Gastos por Categoria", records,
                    r -> r.getCategoryNameSnapshot());
        }
    }

    private void buildPieChart(String title, List<PaymentRecord> records,
                                java.util.function.Function<PaymentRecord, String> keyFn) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (PaymentRecord r : records) {
            String key = keyFn.apply(r);
            if (key == null) key = "Desconhecido";
            totals.merge(key, r.getAmount(), Double::sum);
        }
        historyPieChart.setTitle(title);
        historyPieChart.getData().clear();
        totals.forEach((name, amount) ->
                historyPieChart.getData().add(
                        new PieChart.Data(String.format("%s (R$%.0f)", name, amount), amount)));
    }

    private void updateTotal(List<PaymentRecord> records) {
        double total = records.stream().mapToDouble(PaymentRecord::getAmount).sum();
        totalLabel.setText(String.format("Total: R$%.2f", total));
    }

    // ── Gráfico de linha ─────────────────────────────────────────────────────

    /**
     * Agrega os registros por ciclo (competence) e popula o LineChart.
     * Quando nenhum filtro de data/ciclo é aplicado, garante que pelo menos
     * os últimos DEFAULT_MONTHS meses apareçam no eixo X.
     */
    private void updateTrendChart(List<PaymentRecord> records,
                                  LocalDate startDate, LocalDate endDate,
                                  String competenceFilter) {

        // 1. Somar valores por ciclo a partir dos registros filtrados
        Map<String, Double> totaisPorCiclo = new TreeMap<>();
        for (PaymentRecord r : records) {
            String ciclo = r.getCompetence();
            if (ciclo != null) {
                totaisPorCiclo.merge(ciclo, r.getAmount(), Double::sum);
            }
        }

        // 2. Determinar o intervalo de ciclos a exibir
        List<String> ciclos = buildCicloRange(totaisPorCiclo, startDate, endDate, competenceFilter);

        // 3. Montar série
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Gastos");
        for (String ciclo : ciclos) {
            double valor = totaisPorCiclo.getOrDefault(ciclo, 0.0);
            series.getData().add(new XYChart.Data<>(ciclo, valor));
        }

        trendChart.getData().clear();
        trendChart.getData().add(series);

        // 4. Ajustar legenda do eixo X para se adaptar ao número de ciclos
        trendXAxis.setTickLabelRotation(-45);
        trendXAxis.setTickLabelGap(4);
        // Garante que o eixo reajuste o tamanho da fonte automaticamente
        trendXAxis.setAutoRanging(true);

        // 5. Adicionar tooltips nos símbolos (deve ser feito após adicionar ao gráfico)
        for (XYChart.Data<String, Number> dataPoint : series.getData()) {
            installTooltip(dataPoint);
        }
    }

    /**
     * Constrói a lista ordenada de ciclos YYYY-MM a exibir.
     *
     * Regras de prioridade:
     *  1. Filtro de ciclo único  → exibe apenas aquele mês.
     *  2. Filtro de datas (De/Até) → o eixo X vai exatamente de startDate a endDate
     *     (sem expandir para além das datas escolhidas pelo usuário).
     *  3. Sem filtros → últimos DEFAULT_MONTHS meses; expande se houver dados mais antigos.
     */
    private List<String> buildCicloRange(Map<String, Double> totaisPorCiclo,
                                          LocalDate startDate, LocalDate endDate,
                                          String competenceFilter) {
        List<String> ciclos = new ArrayList<>();

        // 1. Filtro de ciclo único
        if (competenceFilter != null && !competenceFilter.isBlank()) {
            ciclos.add(competenceFilter.trim());
            return ciclos;
        }

        boolean hasDateFilter = (startDate != null || endDate != null);
        YearMonth fim = YearMonth.now();
        YearMonth inicio;

        if (hasDateFilter) {
            // 2. Respeitar exatamente o período escolhido pelo usuário
            inicio = (startDate != null) ? YearMonth.from(startDate) : fim.minusMonths(DEFAULT_MONTHS - 1);
            if (endDate != null) fim = YearMonth.from(endDate);
        } else {
            // 3. Sem filtro: últimos DEFAULT_MONTHS meses, expande se necessário
            inicio = fim.minusMonths(DEFAULT_MONTHS - 1);
            if (!totaisPorCiclo.isEmpty()) {
                YearMonth minData = totaisPorCiclo.keySet().stream()
                        .map(YearMonth::parse)
                        .min(Comparator.naturalOrder())
                        .orElse(inicio);
                YearMonth maxData = totaisPorCiclo.keySet().stream()
                        .map(YearMonth::parse)
                        .max(Comparator.naturalOrder())
                        .orElse(fim);
                if (minData.isBefore(inicio)) inicio = minData;
                if (maxData.isAfter(fim))     fim     = maxData;
            }
        }

        YearMonth cursor = inicio;
        while (!cursor.isAfter(fim)) {
            ciclos.add(cursor.toString());
            cursor = cursor.plusMonths(1);
        }
        return ciclos;
    }

    /**
     * Instala um Tooltip de aparição imediata no nó do símbolo de um ponto do LineChart.
     */
    private void installTooltip(XYChart.Data<String, Number> dataPoint) {
        if (dataPoint.getNode() == null) return;

        double valor = dataPoint.getYValue().doubleValue();
        String ciclo = dataPoint.getXValue();
        String texto = String.format("%s%nR$ %.2f", ciclo, valor);

        Tooltip tooltip = new Tooltip(texto);
        // ── Sem atraso ──────────────────────────────────────────────────────
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setHideDelay(Duration.millis(100));
        tooltip.setShowDuration(Duration.INDEFINITE);
        // ── Estilo ──────────────────────────────────────────────────────────
        tooltip.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-background-color: rgba(20,20,30,0.95);" +
                "-fx-text-fill: #e8e8e8;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 6px 10px;"
        );
        Tooltip.install(dataPoint.getNode(), tooltip);

        // Cursor de mão ao passar sobre o ponto
        dataPoint.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> dataPoint.getNode().setCursor(Cursor.HAND));
        dataPoint.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> dataPoint.getNode().setCursor(Cursor.DEFAULT));
    }

    // ── Handlers de ação ─────────────────────────────────────────────────────

    /** Chamado quando o usuário muda a opção do dropdown do gráfico de pizza. */
    @FXML
    private void handleChartModeChange() {
        updatePieChart(currentRecords);
    }

    @FXML
    private void handleSearch() {
        String subName = subscriptionFilter.getValue();
        String subId = null;
        if (subName != null && !subName.equals("Todas as Assinaturas")) {
            subId = subscriptionNameToId.get(subName);
        }

        String catValue = categoryFilter.getValue();
        String catFilter = (catValue != null && !catValue.equals("Todas as Categorias")) ? catValue : null;
        String pmValue = payMethodFilter.getValue();
        String pmFilter = (pmValue != null && !pmValue.equals("Todos os Métodos")) ? pmValue : null;
        String competence = competenceField.getText();
        LocalDate start = startDatePicker.getValue();
        LocalDate end   = endDatePicker.getValue();

        loadData(subId, catFilter, pmFilter,
                competence.isEmpty() ? null : competence,
                start, end);
    }

    @FXML
    private void handleClear() {
        subscriptionFilter.setValue(null);
        categoryFilter    .setValue(null);
        payMethodFilter   .setValue(null);
        competenceField.clear();
        startDatePicker.setValue(null);
        endDatePicker  .setValue(null);
        loadData(null, null, null, null, null, null);
    }

    @FXML
    private void handleBack() {
        NavigationManager.navigateTo("dashboard.fxml");
    }
}
