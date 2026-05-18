package com.iafitness.aurafitengine.model;


public class GrupoMuscular {
    private int id;
    private String nome;

    public GrupoMuscular() {}

    public GrupoMuscular(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}