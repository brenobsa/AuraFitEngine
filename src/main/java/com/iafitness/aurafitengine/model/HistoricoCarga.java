package com.iafitness.aurafitengine.model;


import java.time.LocalDateTime;

public class HistoricoCarga {
    private int id;
    private Usuario usuario;
    private Exercicio exercicio;
    private LocalDateTime dataRegistro;
    private double cargaUtilizada;
    private int repeticoesFeitas;
    private int seriesFeitas;
    private String tipoTreino;

    public HistoricoCarga() {}

    // Getters e Setters
    public String getTipoTreino() { return tipoTreino; }
    public void setTipoTreino(String tipoTreino) { this.tipoTreino = tipoTreino; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Exercicio getExercicio() { return exercicio; }
    public void setExercicio(Exercicio exercicio) { this.exercicio = exercicio; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
    public double getCargaUtilizada() { return cargaUtilizada; }
    public void setCargaUtilizada(double cargaUtilizada) { this.cargaUtilizada = cargaUtilizada; }
    public int getRepeticoesFeitas() { return repeticoesFeitas; }
    public void setRepeticoesFeitas(int repeticoesFeitas) { this.repeticoesFeitas = repeticoesFeitas; }
    public int getSeriesFeitas() { return seriesFeitas; }
    public void setSeriesFeitas(int seriesFeitas) { this.seriesFeitas = seriesFeitas; }
}