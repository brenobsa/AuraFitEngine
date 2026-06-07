package com.iafitness.aurafitengine.controller;

import com.iafitness.aurafitengine.ai.AiEngine;
import com.iafitness.aurafitengine.model.Exercicio;
import com.iafitness.aurafitengine.model.TemplateItemTreino;
import com.iafitness.aurafitengine.model.TemplatePeriodizacao;
import com.iafitness.aurafitengine.model.Usuario;
import com.iafitness.aurafitengine.model.DivisaoTreino;
import com.iafitness.aurafitengine.repository.TreinoRepository;
import com.iafitness.aurafitengine.repository.DivisaoTreinoRepository;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private ComboBox<DivisaoTreino> comboDivisao;
    @FXML private ComboBox<String> comboNivel;
    @FXML private ComboBox<String> comboTreinoAtual;

    @FXML private TextArea chatArea;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;

    @FXML private TableView<Exercicio> workoutTable;
    @FXML private TableColumn<Exercicio, String> colExercicio;
    @FXML private TableColumn<Exercicio, String> colGrupo;
    @FXML private TableColumn<Exercicio, String> colSeries;
    @FXML private TableColumn<Exercicio, String> colRepeticoes;

    @FXML private Button btnMenu;

    private AiEngine aiEngine;
    private TreinoRepository treinoRepo;
    private Usuario usuarioLogado;
    private ObservableList<Exercicio> listaExerciciosTabela;
    private Map<String, List<Exercicio>> mapaFichasTreino = new HashMap<>();

    // Lista global em memória para guardar todas as planilhas salvas pelo usuário nesta sessão
    public static List<String[]> treinosSalvosCompartilhados = new ArrayList<>();
    static {
        treinosSalvosCompartilhados.add(new String[]{"AB (Superior/Inferior)", "Hipertrofia"});
    }

    @FXML
    public void initialize() {
        this.aiEngine = new AiEngine();
        this.treinoRepo = new TreinoRepository();
        this.listaExerciciosTabela = FXCollections.observableArrayList();

        this.usuarioLogado = new Usuario();
        this.usuarioLogado.setId(1);

        DivisaoTreinoRepository divisaoRepo = new DivisaoTreinoRepository();
        List<DivisaoTreino> divisoesDoBanco = divisaoRepo.buscarTodas();
        if (divisoesDoBanco != null && !divisoesDoBanco.isEmpty()) {
            comboDivisao.setItems(FXCollections.observableArrayList(divisoesDoBanco));
            comboDivisao.setValue(divisoesDoBanco.get(0));
        }

        comboNivel.setItems(FXCollections.observableArrayList("Hipertrofia", "Força", "Resistência", "Emagrecimento"));
        comboNivel.setValue("Hipertrofia");

        comboTreinoAtual.setItems(FXCollections.observableArrayList("A"));
        comboTreinoAtual.setValue("A");

        colExercicio.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colSeries.setCellValueFactory(new PropertyValueFactory<>("seriesPrescritas"));
        colRepeticoes.setCellValueFactory(new PropertyValueFactory<>("repeticoesPrescritas"));

        colGrupo.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getGrupoMuscular() != null) {
                return new SimpleStringProperty(cellData.getValue().getGrupoMuscular().getNome());
            }
            return new SimpleStringProperty("-");
        });

        workoutTable.setItems(listaExerciciosTabela);

        // MODIFICAÇÃO: Criação do menu suspenso dinâmico para as 3 barrinhas
        ContextMenu menuSuspenso = new ContextMenu();
        MenuItem itemFichas = new MenuItem("📋 Minhas Fichas");
        MenuItem itemEstatistica = new MenuItem("📊 Estatísticas de Carga");

        // Vincula as ações de clique de cada opção do menu
        itemFichas.setOnAction(e -> handleNavegarParaMinhasFichas());
        itemEstatistica.setOnAction(e -> handleNavegarParaEstatiticas());

        menuSuspenso.getItems().addAll(itemFichas, itemEstatistica);

        // Faz o menu aparecer logo abaixo do botão ao clicar nele
        btnMenu.setOnMouseClicked(event -> {
            menuSuspenso.show(btnMenu, event.getScreenX(), event.getScreenY());
        });
    }

    @FXML
    private void handleIniciarRotina() {
        DivisaoTreino divisaoObjeto = comboDivisao.getValue();
        String tipoTreino = comboNivel.getValue();
        if (divisaoObjeto == null || tipoTreino == null) return;

        String divisaoNome = divisaoObjeto.getNome();
        chatArea.clear();
        mapaFichasTreino.clear();

        atualizarAbasVisuais(divisaoNome);

        chatArea.appendText("AuraFit Engine: Consultando banco de dados para " + divisaoNome + "...\n");
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                TemplatePeriodizacao template = treinoRepo.buscarTemplatePorDiretrizes(divisaoNome, tipoTreino);

                if (template == null || template.getItens() == null || template.getItens().isEmpty()) {
                    Platform.runLater(() -> {
                        chatArea.appendText("Erro: Template não populado ou não encontrado no Banco para esta combinação.\n");
                        sendButton.setDisable(false);
                    });
                    return;
                }

                for (TemplateItemTreino item : template.getItens()) {
                    String letra = item.getFichaLetra().toUpperCase();
                    mapaFichasTreino.computeIfAbsent(letra, k -> new ArrayList<>()).add(item.getExercicio());
                }

                Platform.runLater(this::handleAlternarTreinoVisual);

                String tempoFormatado = template.getTempoDescansoS() + " segundos";
                if (template.getTempoDescansoS() == 90) tempoFormatado = "90seg ou 1min e 30seg";
                else if (template.getTempoDescansoS() == 150) tempoFormatado = "150seg ou 2min e 30seg";
                else if (template.getTempoDescansoS() == 180) tempoFormatado = "180seg ou 3 minutos";
                else if (template.getTempoDescansoS() == 240) tempoFormatado = "240seg ou 4 minutos";

                String vezesSemana = "3 a 5 vezes";
                if (divisaoNome.contains("AB (")) vezesSemana = "4 vezes";
                else if (divisaoNome.contains("ABC")) vezesSemana = "3 a 6 vezes";
                else if (divisaoNome.contains("ABCD")) vezesSemana = "4 vezes";
                else if (divisaoNome.contains("FULLBODY")) vezesSemana = "3 vezes";
                else if (divisaoNome.contains("ABAB")) vezesSemana = "4 vezes";

                String baseConhecimentoIA = "";
                if (tipoTreino.equalsIgnoreCase("Força")) {
                    baseConhecimentoIA = "Serve para aumentar a capacidade do sistema nervoso e dos músculos de levantarem cargas máximas. Sua função é estimular o recrutamento de mais fibras musculares ao mesmo tempo.";
                } else if (tipoTreino.equalsIgnoreCase("Hipertrofia")) {
                    baseConhecimentoIA = "Serve para ganhar massa muscular, volume e desenhar o formato do corpo esteticamente. Sua função é gerar microlesões controladas nas fibras.";
                } else if (tipoTreino.equalsIgnoreCase("Resistência")) {
                    baseConhecimentoIA = "Serve para melhorar o fôlego dos músculos e a capacidade de fazer esforço por muito tempo sem cansar.";
                } else if (tipoTreino.equalsIgnoreCase("Emagrecimento")) {
                    baseConhecimentoIA = "Serve para acelerar a queima de gordura corporal, mantendo a massa magra e tonificando o corpo.";
                }

                String promptReescrita =
                        "Você é o AuraFit Coach. Reescreva de forma curta e motivadora em até 20 palavras:\n" + baseConhecimentoIA;

                String fraseReformulada = aiEngine.enviarMensagemLivre(promptReescrita).trim();

                String resultadoFinal =
                        template.getTipoEstimulo() + ": " + fraseReformulada + "\n\n" +
                                "Divisao de treino: " + template.getNomeDivisao() + "\n\n" +
                                "Tempo de descanso: " + tempoFormatado + "\n\n" +
                                "Vezes por semana: " + vezesSemana;

                Platform.runLater(() -> {
                    chatArea.setText("AuraFit Personal Trainer:\n" + resultadoFinal + "\n\n-----------------------\n");
                    sendButton.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    chatArea.appendText("Erro interno ao processar.\n");
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    private void atualizarAbasVisuais(String d) {
        comboTreinoAtual.getItems().clear();
        if (d.contains("AB (")) comboTreinoAtual.getItems().addAll("A", "B");
        else if (d.contains("ABC")) comboTreinoAtual.getItems().addAll("A", "B", "C");
        else if (d.contains("ABCD")) comboTreinoAtual.getItems().addAll("A", "B", "C", "D");
        else if (d.contains("ABAB")) comboTreinoAtual.getItems().addAll("A", "B");
        else comboTreinoAtual.getItems().add("A");
        comboTreinoAtual.setValue("A");
    }

    @FXML
    private void handleAlternarTreinoVisual() {
        String ficha = comboTreinoAtual.getValue();
        if (ficha == null) return;

        listaExerciciosTabela.clear();
        List<Exercicio> listaFiltrada = mapaFichasTreino.get(ficha);
        if (listaFiltrada != null) {
            listaExerciciosTabela.setAll(listaFiltrada);
        }
    }

    @FXML
    private void handleSendAction() {
        String input = userInputField.getText().trim();
        if (input.isEmpty()) return;

        chatArea.appendText("Você: " + input + "\n");
        userInputField.clear();

        new Thread(() -> {
            String promptMapeado = "Você é o AuraFit Coach. Responda curto: " + input;
            String resp = aiEngine.enviarMensagemLivre(promptMapeado);
            Platform.runLater(() -> chatArea.appendText("AuraFit: " + resp + "\n\n"));
        }).start();
    }

    @FXML
    private void handleSaveWorkout() {
        DivisaoTreino divisaoObjeto = comboDivisao.getValue();
        String tipoTreino = comboNivel.getValue();

        if (divisaoObjeto != null && tipoTreino != null) {
            boolean jaExiste = false;
            for (String[] t : treinosSalvosCompartilhados) {
                if (t[0].equals(divisaoObjeto.getNome()) && t[1].equals(tipoTreino)) {
                    jaExiste = true;
                    break;
                }
            }
            if (!jaExiste) {
                treinosSalvosCompartilhados.add(new String[]{divisaoObjeto.getNome(), tipoTreino});
            }
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso");
        a.setHeaderText(null);
        a.setContentText("Rotina sincronizada com o Banco de Templates com sucesso!");
        a.showAndWait();
        handleNavegarParaMinhasFichas();
    }

    // MODIFICAÇÃO: Separação clara dos dois caminhos de telas independentes
    private void handleNavegarParaMinhasFichas() {
        direcionarParaTela("/com/iafitness/aurafitengine/minhas-fichas-view.fxml", "AuraFit Engine - Minhas Fichas");
    }

    private void handleNavegarParaEstatiticas() {
        direcionarParaTela("/com/iafitness/aurafitengine/evolucao-cargas-view.fxml", "AuraFit Engine - Centro de Estatísticas");
    }

    private void direcionarParaTela(String fxmlPath, String titulo) {
        java.net.URL url = getClass().getResource(fxmlPath);
        if (url == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) chatArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}