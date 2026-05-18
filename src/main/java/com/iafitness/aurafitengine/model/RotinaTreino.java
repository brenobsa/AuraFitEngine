package com.iafitness.aurafitengine.model;


import java.time.LocalDateTime;

public class RotinaTreino {
    private int id;
    private Usuario usuario; // Associa ao usuário dono do treino
    private String nomeRotina;
    private LocalDateTime dataCriacao;
    private boolean ativa;

    public RotinaTreino() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNomeRotina() { return nomeRotina; }
    public void setNomeRotina(String nomeRotina) { this.nomeRotina = nomeRotina; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}