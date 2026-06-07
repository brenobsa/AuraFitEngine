package com.iafitness.aurafitengine.model;

import java.util.List;

public class TemplatePeriodizacao {
    private int id;
    private String nomeDivisao;    // Mapeia 'AB (Superior/Inferior)', 'ABC (Sinergia)', etc.
    private String tipoEstimulo;   // Mapeia 'Hipertrofia', 'Força', 'Resistência'
    private int tempoDescansoS;    // Tempo em segundos (45, 60, 90, 120)
    private List<TemplateItemTreino> itens;

    // Constructor Padrão
    public TemplatePeriodizacao() {}

    // Getters e Setters Completos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeDivisao() { return nomeDivisao; }
    public void setNomeDivisao(String nomeDivisao) { this.nomeDivisao = nomeDivisao; }

    public String getTipoEstimulo() { return tipoEstimulo; }
    public void setTipoEstimulo(String tipoEstimulo) { this.tipoEstimulo = tipoEstimulo; }

    public int getTempoDescansoS() { return tempoDescansoS; }
    public void setTempoDescansoS(int tempoDescansoS) { this.tempoDescansoS = tempoDescansoS; }

    public List<TemplateItemTreino> getItens() { return itens; }
    public void setItens(List<TemplateItemTreino> itens) { this.itens = itens; }
}