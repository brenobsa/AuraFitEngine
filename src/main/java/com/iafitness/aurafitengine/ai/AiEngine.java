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

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiEngine() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String enviarMensagem(String userMessage, List<Exercicio> exerciciosDisponiveis) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "qwen2.5:3b");
            requestBody.put("stream", false);

            StringBuilder listaContexto = new StringBuilder();
            for (Exercicio ex : exerciciosDisponiveis) {
                listaContexto.append("- ").append(ex.getNome())
                        .append(" (Foco: ").append(ex.getFocoAnatomico())
                        .append(", Tipo: ").append(ex.getTipo())
                        .append(", Dificuldade: ").append(ex.getDificuldade()).append(")\n");
            }

            String systemPrompt =
                    "Você é o AuraFit Engine, um personal trainer de elite integrado ao banco de dados do sistema.\n" +
                            "Regra Absoluta: Você SÓ pode escolher exercícios que estão na lista fornecida abaixo. " +
                            "Não invente nenhum nome fora do catálogo enviado.\n\n" +
                            "EXERCÍCIOS DISPONÍVEIS NO BANCO DE DADOS:\n" + listaContexto.toString() + "\n" +
                            "Instruções Obrigatórias de Resposta:\n" +
                            "1. Faça uma breve introdução em texto amigável e direto listando a rotina gerada.\n" +
                            "2. No final da resposta, gere OBRIGATORIAMENTE um único bloco JSON englobado estritamente pelas tags ```json e ```.\n" +
                            "Cada objeto do array JSON deve conter a chave \"treino\" indicando a qual letra de ficha (A, B, C ou D) aquele exercício pertence.\n\n" +
                            "Exemplo exato de estrutura do JSON:\n" +
                            "```json\n" +
                            "[\n" +
                            "  { \"nome\": \"Nome Exato do Exercício\", \"foco\": \"Foco\", \"tipo\": \"Tipo\", \"dificuldade\": \"Dificuldade\", \"treino\": \"A\" }\n" +
                            "]\n" +
                            "```\n" +
                            "Selecione de 3 a 5 exercícios reais para cada letra de ficha exigida.";

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
}