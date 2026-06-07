package com.iafitness.aurafitengine.model;

public class TemplateItemTreino {
    private int id;
    private int templateId;
    private Exercicio exercicio; // Composição relacional direta com o Exercício da base
    private String fichaLetra;   // Armazena 'A', 'B', 'C', 'D'
    private int seriesSugeridas;
    private String repeticoesSugeridas;

    // Constructor Padrão
    public TemplateItemTreino() {}

    // Getters e Setters Completos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTemplateId() { return templateId; }
    public void setTemplateId(int templateId) { this.templateId = templateId; }

    public Exercicio getExercicio() { return exercicio; }
    public void setExercicio(Exercicio exercicio) { this.exercicio = exercicio; }

    public String getFichaLetra() { return fichaLetra; }
    public void setFichaLetra(String fichaLetra) { this.fichaLetra = fichaLetra; }

    public int getSeriesSugeridas() { return seriesSugeridas; }
    public void setSeriesSugeridas(int seriesSugeridas) { this.seriesSugeridas = seriesSugeridas; }

    public String getRepeticoesSugeridas() { return repeticoesSugeridas; }
    public void setRepeticoesSugeridas(String repeticoesSugeridas) { this.repeticoesSugeridas = repeticoesSugeridas; }
}