package com.iafitness.aurafitengine.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iafitness.aurafitengine.model.Exercicio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AiEngine {

    // Modelo atualizado para a versão de 0.5B parâmetros do Qwen 2.5
    private static final String MODEL_NAME = "qwen2.5:1.5b";
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiEngine() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Geração de Rotinas Estritas com Contexto RAG.
     * Otimizado para modelos menores (0.5B).
     */
    public String enviarMensagem(String userMessage, List<Exercicio> exerciciosDisponiveis) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("stream", false);

            StringBuilder listaContexto = new StringBuilder();
            for (Exercicio ex : exerciciosDisponiveis) {
                listaContexto.append("- ").append(ex.getNome())
                        .append(" (Foco: ").append(ex.getFocoAnatomico())
                        .append(", Tipo: ").append(ex.getTipo())
                        .append(", Dificuldade: ").append(ex.getDificuldade()).append(")\n");
            }

            // IMPORTANTE: Prompt simplificado e agressivo contra alucinações para o modelo de 0.5B
            String systemPrompt =
                    "Você é um gerador de dados restrito. Você NÃO conversa. Você APENAS gera JSON.\n\n" +
                            "REGRA CRÍTICA: Use APENAS os exercícios da lista abaixo. Não invente nenhum nome.\n" +
                            "EXERCÍCIOS DISPONÍVEIS:\n" + listaContexto.toString() + "\n" +
                            "INSTRUÇÃO DE SAÍDA:\n" +
                            "Gere APENAS um array JSON válido dentro de blocos ```json. Não escreva nenhuma introdução ou explicação.\n" +
                            "Distribua os exercícios entre as fichas (A, B, C, D) no campo \"treino\".\n\n" +
                            "Formato exigido:\n" +
                            "```json\n" +
                            "[\n" +
                            "  { \"nome\": \"Nome Exato\", \"foco\": \"Foco\", \"tipo\": \"Tipo\", \"dificuldade\": \"Dificuldade\", \"treino\": \"A\" }\n" +
                            "]\n" +
                            "```";

            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode systemNode = objectMapper.createObjectNode();
            systemNode.put("role", "system");
            systemNode.put("content", systemPrompt);
            messagesArray.add(systemNode);

            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
            messagesArray.add(userNode);

            requestBody.set("messages", messagesArray);
            String payload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
                return responseJson.get("message").get("content").asText();
            } else {
                return "Erro de comunicação com o Ollama local. Código HTTP: " + response.statusCode();
            }

        } catch (Exception e) {
            return "Erro crítico no motor de IA (AiEngine): " + e.getMessage();
        }
    }

    /**
     * Consultoria em Texto Livre.
     */
    public String enviarMensagemLivre(String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("stream", false);

            ArrayNode messagesArray = objectMapper.createArrayNode();

            ObjectNode systemNode = objectMapper.createObjectNode();
            systemNode.put("role", "system");
            systemNode.put("content", "Você é o assistente fitness AuraFit. Responda de forma direta, curta e motivadora. Seja objetivo.");
            messagesArray.add(systemNode);

            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
            messagesArray.add(userNode);

            requestBody.set("messages", messagesArray);
            String payload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
                return responseJson.get("message").get("content").asText();
            }
            return "Erro de comunicação (Código " + response.statusCode() + ")";
        } catch (Exception e) {
            return "Erro no motor de texto livre: " + e.getMessage();
        }
    }
}