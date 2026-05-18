package com.iafitness.aurafitengine.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iafitness.aurafitengine.ai.AiEngine;
import com.iafitness.aurafitengine.model.Exercicio;
import com.iafitness.aurafitengine.model.HistoricoCarga;
import com.iafitness.aurafitengine.model.Usuario;
import com.iafitness.aurafitengine.repository.ExercicioRepository;
import com.iafitness.aurafitengine.repository.TreinoRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MainController {

    @FXML private ComboBox<String> comboDivisao;
    @FXML private TextArea chatArea;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;

    @FXML private ComboBox<String> comboTreinoAtual;
    @FXML private TableView<Exercicio> workoutTable;
    @FXML private TableColumn<Exercicio, String> colExercicio;
    @FXML private TableColumn<Exercicio, String> colFoco;
    @FXML private TableColumn<Exercicio, String> colTipo;
    @FXML private TableColumn<Exercicio, String> colDificuldade;

    @FXML private TextField txtCarga;
    @FXML private TextField txtRepeticoes;
    @FXML private TextField txtSeries;
    @FXML private Label lblExercicioSelecionado;
    @FXML private Button btnRegistrarCarga;

    private AiEngine aiEngine;
    private ExercicioRepository exercicioRepo;
    private TreinoRepository treinoRepo;
    private Exercicio exercicioSelecionado;
    private Usuario usuarioLogado;
    private ObservableList<Exercicio> listaExerciciosTabela;

    private Map<String, List<Exercicio>> mapaFichasTreino;
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        this.aiEngine = new AiEngine();
        this.exercicioRepo = new ExercicioRepository();
        this.treinoRepo = new TreinoRepository();
        this.listaExerciciosTabela = FXCollections.observableArrayList();
        this.mapaFichasTreino = new HashMap<>();

        this.usuarioLogado = new Usuario();
        this.usuarioLogado.setId(1);

        comboDivisao.setItems(FXCollections.observableArrayList("AB", "ABC", "ABAB", "ABCD", "Fullbody"));
        comboDivisao.setValue("ABC");

        comboTreinoAtual.setItems(FXCollections.observableArrayList("A"));
        comboTreinoAtual.setValue("A");

        colExercicio.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colFoco.setCellValueFactory(new PropertyValueFactory<>("focoAnatomico"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDificuldade.setCellValueFactory(new PropertyValueFactory<>("dificuldade"));

        workoutTable.setItems(listaExerciciosTabela);
        btnRegistrarCarga.setDisable(true);

        chatArea.appendText("AuraFit Engine: Selecione uma divisão de treino acima para inicializar.\n");

        workoutTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                exercicioSelecionado = newSelection;
                lblExercicioSelecionado.setText("Registrar para: " + exercicioSelecionado.getNome());
                btnRegistrarCarga.setDisable(false);
            }
        });
    }

    @FXML
    private void handleIniciarRotina() {
        String divisao = comboDivisao.getValue();
        chatArea.clear();
        listaExerciciosTabela.clear();
        mapaFichasTreino.clear();
        btnRegistrarCarga.setDisable(true);
        lblExercicioSelecionado.setText("Selecione um exercício acima para registrar");

        atualizarOpcoesSeletorTreino(divisao);

        chatArea.appendText("AuraFit Engine: Conectando ao MySQL e carregando catálogo de exercícios...\n");
        chatArea.appendText("A estruturar rotina [" + divisao + "] biomecanicamente ideal... Aguarde.\n\n");
        sendButton.setDisable(true);

        String prompt = "Monte uma rotina completa dividida exatamente na estrutura " + divisao + ". " +
                "Distribua os exercícios de forma correta e informe a letra da ficha correspondente de cada exercício no JSON.";

        new Thread(() -> {
            try {
                List<Exercicio> contextoBanco = new ArrayList<>();
                contextoBanco.addAll(exercicioRepo.buscarPorGrupoMuscular("Peito"));
                contextoBanco.addAll(exercicioRepo.buscarPorGrupoMuscular("Costas"));
                contextoBanco.addAll(exercicioRepo.buscarPorGrupoMuscular("Pernas"));
                contextoBanco.addAll(exercicioRepo.buscarPorGrupoMuscular("Bíceps"));

                String respostaIA = aiEngine.enviarMensagem(prompt, contextoBanco);
                processarRespostaIA(respostaIA);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatArea.appendText("Erro ao carregar contexto RAG do banco: " + e.getMessage() + "\n");
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleSendAction() {
        String input = userInputField.getText().trim();
        if (input.isEmpty()) return;

        chatArea.appendText("Você: " + input + "\n");
        userInputField.clear();
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                String inputNormalizado = normalizarTexto(input);
                String grupoAlvo = "Peito";

                if (inputNormalizado.contains("costas")) {
                    grupoAlvo = "Costas";
                } else if (inputNormalizado.contains("perna") || inputNormalizado.contains("coxa") || inputNormalizado.contains("inferiores")) {
                    grupoAlvo = "Pernas";
                } else if (inputNormalizado.contains("biceps") || inputNormalizado.contains("braco")) {
                    grupoAlvo = "Bíceps";
                }

                List<Exercicio> exerciciosDoBanco = exercicioRepo.buscarPorGrupoMuscular(grupoAlvo);
                String respostaIA = aiEngine.enviarMensagem(input, exerciciosDoBanco);
                processarRespostaIA(respostaIA);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatArea.appendText("Erro no fluxo do chat: " + e.getMessage() + "\n");
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    private void processarRespostaIA(String respostaCompleta) {
        String textoExplicativo = respostaCompleta;
        String jsonPuro = "";

        try {
            if (respostaCompleta.contains("```json") && respostaCompleta.contains("```")) {
                int inicioJson = respostaCompleta.indexOf("```json") + 7;
                int fimJson = respostaCompleta.indexOf("```", inicioJson);
                jsonPuro = respostaCompleta.substring(inicioJson, fimJson).trim();
                textoExplicativo = respostaCompleta.substring(0, respostaCompleta.indexOf("```json")).trim();
            }

            Map<String, List<Exercicio>> novosTreinos = new HashMap<>();

            if (!jsonPuro.isEmpty()) {
                JsonNode raiz = mapper.readTree(jsonPuro);
                if (raiz.isArray()) {
                    for (JsonNode node : raiz) {
                        if (node.has("nome") && !node.get("nome").isNull()) {
                            String nomeEx = node.get("nome").asText();
                            Exercicio exDoBanco = exercicioRepo.buscarPorNome(nomeEx);

                            if (exDoBanco != null) {
                                String letraTreino = "A";
                                if (node.has("treino") && !node.get("treino").isNull()) {
                                    letraTreino = node.get("treino").asText().toUpperCase().trim();
                                }
                                novosTreinos.computeIfAbsent(letraTreino, k -> new ArrayList<>()).add(exDoBanco);
                            }
                        }
                    }
                }
            }

            final String textoParaExibir = textoParaExibirTratado(textoExplicativo);
            Platform.runLater(() -> {
                chatArea.appendText("AuraFit:\n" + textoParaExibir + "\n\n");
                if (!novosTreinos.isEmpty()) {
                    this.mapaFichasTreino.putAll(novosTreinos);
                    handleAlternarTreinoVisual();
                    chatArea.appendText("SISTEMA: Exercícios validados mapeados na sua grade atual!\n\n");
                }
                sendButton.setDisable(false);
            });

        } catch (Exception e) {
            Platform.runLater(() -> {
                chatArea.appendText("AuraFit:\n" + respostaCompleta + "\n\n");
                sendButton.setDisable(false);
            });
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAlternarTreinoVisual() {
        String fichaSelecionada = comboTreinoAtual.getValue();
        if (fichaSelecionada == null) return;

        listaExerciciosTabela.clear();
        btnRegistrarCarga.setDisable(true);
        lblExercicioSelecionado.setText("Selecione um exercício acima para registrar");

        List<Exercicio> exerciciosDaFicha = mapaFichasTreino.get(fichaSelecionada);
        if (exerciciosDaFicha != null && !exerciciosDaFicha.isEmpty()) {
            listaExerciciosTabela.setAll(exerciciosDaFicha);
        }
    }

    private void atualizarOpcoesSeletorTreino(String divisao) {
        Platform.runLater(() -> {
            comboTreinoAtual.getItems().clear();
            switch (divisao.toUpperCase()) {
                case "AB":
                case "ABAB":
                    comboTreinoAtual.setItems(FXCollections.observableArrayList("A", "B"));
                    break;
                case "ABC":
                    comboTreinoAtual.setItems(FXCollections.observableArrayList("A", "B", "C"));
                    break;
                case "ABCD":
                    comboTreinoAtual.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
                    break;
                default:
                    comboTreinoAtual.setItems(FXCollections.observableArrayList("A"));
                    break;
            }
            comboTreinoAtual.setValue("A");
        });
    }

    @FXML
    private void handleRegistrarCarga() {
        if (exercicioSelecionado == null || txtCarga.getText().isEmpty() || txtRepeticoes.getText().isEmpty() || txtSeries.getText().isEmpty()) {
            exibirAlerta("Dados Vazios", "Preencha todas as caixas para salvar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double carga = Double.parseDouble(txtCarga.getText().replace(",", "."));
            int reps = Integer.parseInt(txtRepeticoes.getText());
            int series = Integer.parseInt(txtSeries.getText());

            HistoricoCarga historico = new HistoricoCarga();
            historico.setUsuario(usuarioLogado);
            historico.setExercicio(exercicioSelecionado);
            historico.setCargaUtilizada(carga);
            historico.setRepeticoesFeitas(reps);
            historico.setSeriesFeitas(series);
            historico.setDataRegistro(LocalDateTime.now());

            boolean sucesso = treinoRepo.registrarCarga(historico);

            if (sucesso) {
                exibirAlerta("Sucesso", "Métrica de carga salva no MySQL!", Alert.AlertType.INFORMATION);
                txtCarga.clear();
                txtRepeticoes.clear();
                txtSeries.clear();
            } else {
                exibirAlerta("Erro", "Falha interna ao persistir dados de carga.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            exibirAlerta("Formato Inválido", "Séries/Reps devem ser inteiros e Carga aceita decimais.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveWorkout() {
        if (listaExerciciosTabela.isEmpty()) {
            exibirAlerta("Aviso", "Não há registros na tabela ativa para salvar.", Alert.AlertType.WARNING);
            return;
        }
        exibirAlerta("Sucesso", "Ficha gravada com êxito!", Alert.AlertType.INFORMATION);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String termoLimpo = texto.toLowerCase().trim();
        String normalizado = Normalizer.normalize(termoLimpo, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizado).replaceAll("");
    }

    private String textoParaExibirTratado(String texto) {
        if (texto.trim().endsWith("---") || texto.trim().endsWith("-----------------------")) {
            return texto.trim();
        }
        return texto.trim() + "\n\n-----------------------";
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}