package com.iafitness.aurafitengine.controller;

import com.iafitness.aurafitengine.ai.AiEngine;
import com.iafitness.aurafitengine.model.Exercicio;
import com.iafitness.aurafitengine.model.HistoricoCarga;
import com.iafitness.aurafitengine.model.TemplateItemTreino;
import com.iafitness.aurafitengine.model.TemplatePeriodizacao;
import com.iafitness.aurafitengine.repository.HistoricoCargaRepository;
import com.iafitness.aurafitengine.repository.TreinoRepository;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EvolucaoCargasController {

    @FXML private ListView<String> listExerciciosFiltro;
    @FXML private Label lblTituloGrafico;
    @FXML private LineChart<String, Number> chartEvolucao;
    @FXML private TextArea txtAnaliseIA;
    @FXML private ComboBox<String> comboFiltroObjetivo; // Injetado!

    @FXML private TableView<HistoricoCarga> tableHistoricoCargas;
    @FXML private TableColumn<HistoricoCarga, String> colDataRegistro;
    @FXML private TableColumn<HistoricoCarga, String> colCargaRegistrada;
    @FXML private TableColumn<HistoricoCarga, String> colCargaTotal;
    @FXML private TableColumn<HistoricoCarga, Integer> colSeriesFeitas;
    @FXML private TableColumn<HistoricoCarga, Integer> colRepsFeitas;

    private AiEngine aiEngine;
    private TreinoRepository treinoRepo;
    private HistoricoCargaRepository historicoRepo;
    private List<Exercicio> listaExerciciosMapeados = new ArrayList<>();
    private ObservableList<HistoricoCarga> listaTabelaHistorico;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        this.aiEngine = new AiEngine();
        this.treinoRepo = new TreinoRepository();
        this.historicoRepo = new HistoricoCargaRepository();
        this.listaTabelaHistorico = FXCollections.observableArrayList();

        // Configura as opções de filtro
        comboFiltroObjetivo.setItems(FXCollections.observableArrayList("Hipertrofia", "Força"));
        comboFiltroObjetivo.setValue("Hipertrofia");

        colCargaRegistrada.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCargaUtilizada() + " kg"));
        colSeriesFeitas.setCellValueFactory(new PropertyValueFactory<>("seriesFeitas"));
        colRepsFeitas.setCellValueFactory(new PropertyValueFactory<>("repeticoesFeitas"));

        colCargaTotal.setCellValueFactory(cellData -> {
            HistoricoCarga hc = cellData.getValue();
            if (hc != null) {
                double total = hc.getCargaUtilizada() * hc.getRepeticoesFeitas();
                return new SimpleStringProperty(String.format("%.1f kg", total));
            }
            return new SimpleStringProperty("0.0 kg");
        });

        colDataRegistro.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataRegistro() != null) {
                return new SimpleStringProperty(cellData.getValue().getDataRegistro().format(formatter));
            }
            return new SimpleStringProperty("-");
        });

        tableHistoricoCargas.setItems(listaTabelaHistorico);

        carregarListaDeExerciciosDoBanco();

        // Ouvinte para mudança de exercício
        listExerciciosFiltro.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            acionarAtualizacaoFiltro();
        });

        // Ouvinte para mudança de objetivo no ComboBox (Isola os dados)
        comboFiltroObjetivo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            acionarAtualizacaoFiltro();
        });
    }

    private void acionarAtualizacaoFiltro() {
        int idx = listExerciciosFiltro.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < listaExerciciosMapeados.size()) {
            atualizarGraficoETabela(listaExerciciosMapeados.get(idx));
        }
    }

    private void carregarListaDeExerciciosDoBanco() {
        new Thread(() -> {
            try {
                TemplatePeriodizacao template = treinoRepo.buscarTemplatePorDiretrizes("AB (Superior/Inferior)", "Hipertrofia");
                if (template != null && template.getItens() != null) {
                    List<String> nomesExercicios = new ArrayList<>();
                    listaExerciciosMapeados.clear();

                    for (TemplateItemTreino item : template.getItens()) {
                        nomesExercicios.add(item.getExercicio().getNome());
                        listaExerciciosMapeados.add(item.getExercicio());
                    }

                    Platform.runLater(() -> {
                        listExerciciosFiltro.setItems(FXCollections.observableArrayList(nomesExercicios));
                        if (!listExerciciosFiltro.getItems().isEmpty()) {
                            listExerciciosFiltro.getSelectionModel().select(0);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void atualizarGraficoETabela(Exercicio ex) {
        String objetivoFiltro = comboFiltroObjetivo.getValue();
        lblTituloGrafico.setText("Progressão (" + objetivoFiltro + "): " + ex.getNome());
        txtAnaliseIA.setText("AuraFit Coach: Compilando métricas isoladas...");

        new Thread(() -> {
            List<HistoricoCarga> registrosCompletos = historicoRepo.buscarPorUsuario(1);
            List<HistoricoCarga> registrosFiltradosDoExercicio = new ArrayList<>();

            if (registrosCompletos != null) {
                for (HistoricoCarga hc : registrosCompletos) {
                    // FILTRAGEM DUPLA SEGURA: Filtra por id de exercício e por tipo_treino salvo no banco!
                    if (hc.getExercicio() != null && hc.getExercicio().getId() == ex.getId() &&
                            hc.getTipoTreino() != null && hc.getTipoTreino().equalsIgnoreCase(objetivoFiltro)) {
                        registrosFiltradosDoExercicio.add(hc);
                    }
                }
            }

            StringBuilder stringVetorProgresso = new StringBuilder();
            for (HistoricoCarga hc : registrosFiltradosDoExercicio) {
                double tonelagemContada = hc.getCargaUtilizada() * hc.getRepeticoesFeitas();
                stringVetorProgresso.append("- ")
                        .append(hc.getDataRegistro().format(formatter))
                        .append(": ")
                        .append(hc.getCargaUtilizada())
                        .append(" kg (Séries: ")
                        .append(hc.getSeriesFeitas())
                        .append(" | Repetições: ")
                        .append(hc.getRepeticoesFeitas())
                        .append(" | Volume total: ")
                        .append(tonelagemContada)
                        .append(" kg)\n");
            }

            String analiseIAFinal;
            if (!registrosFiltradosDoExercicio.isEmpty()) {
                String promptAnaliseCurva = "Você é o AuraFit Coach. Analise a evolução de sobrecarga no objetivo " + objetivoFiltro + " para o exercício '" + ex.getNome() + "':\n" +
                        stringVetorProgresso.toString() + "\n" +
                        "Forneça um feedback técnico curto (máximo 2 frases) avaliando se a relação de intensidade e volume total de tonelagem está coerente.";
                analiseIAFinal = aiEngine.enviarMensagemLivre(promptAnaliseCurva);
            } else {
                analiseIAFinal = "AuraFit Coach: Nenhum registro localizado para " + ex.getNome() + " no modo de treino " + objetivoFiltro + ". Registre pesos nesta planilha temporal.";
            }

            Platform.runLater(() -> {
                listaTabelaHistorico.setAll(registrosFiltradosDoExercicio);

                chartEvolucao.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                DateTimeFormatter formatoEixoX = DateTimeFormatter.ofPattern("dd/MM");

                for (HistoricoCarga hc : registrosFiltradosDoExercicio) {
                    String dataEixoX = hc.getDataRegistro().format(formatoEixoX);
                    double pesoEixoY = hc.getCargaUtilizada();
                    series.getData().add(new XYChart.Data<>(dataEixoX, pesoEixoY));
                }

                chartEvolucao.getData().add(series);
                txtAnaliseIA.setText(analiseIAFinal);

                if (series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke: #bbf246; -fx-stroke-width: 3px;");
                }
            });
        }).start();
    }

    @FXML
    private void handleVoltarDashboard() {
        String caminhoFxml = "/com/iafitness/aurafitengine/main-view.fxml";
        java.net.URL url = getClass().getResource(caminhoFxml);
        if (url == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) listExerciciosFiltro.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AuraFit Engine - Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}