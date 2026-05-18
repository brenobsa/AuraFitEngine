package com.iafitness.aurafitengine.model;


public class Exercicio {
    private int id;
    private String nome;
    private GrupoMuscular grupoMuscular; // Relacionamento OO
    private String focoAnatomico;
    private String dificuldade; // Pode ser String ou Enum
    private String tipo;        // Composto ou Isolado
    private String descricao;
    private String execucao;

    public Exercicio() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public GrupoMuscular getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(GrupoMuscular grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public String getFocoAnatomico() { return focoAnatomico; }
    public void setFocoAnatomico(String focoAnatomico) { this.focoAnatomico = focoAnatomico; }
    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getExecucao() { return execucao; }
    public void setExecucao(String execucao) { this.execucao = execucao; }
}