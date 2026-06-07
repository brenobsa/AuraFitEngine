package com.iafitness.aurafitengine.model;

public class DivisaoTreino {
    private int id;
    private String nome;
    private int frequenciaSemanal;
    private String descricao;

    public DivisaoTreino() {}

    public DivisaoTreino(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getFrequenciaSemanal() { return frequenciaSemanal; }
    public void setFrequenciaSemanal(int frequenciaSemanal) { this.frequenciaSemanal = frequenciaSemanal; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    /**
     * IMPORTANTE: O ComboBox do JavaFX usa o toString() para renderizar o texto na tela.
     * Sobrescrevendo assim, o seletor exibirá lindamente "ABC" ou "Fullbody".
     */
    @Override
    public String toString() {
        return this.nome;
    }
}