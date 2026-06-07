package com.iafitness.aurafitengine.model;

public class Exercicio {
    private int id;
    private String nome;
    private GrupoMuscular grupoMuscular;
    private String focoAnatomico;
    private String dificuldade;
    private String tipo;
    private String descricao;
    private String execucao;

    // =========================================================================
    // ATRIBUTOS ADICIONADOS PARA INTEGRAÇÃO COM O BANCO DO TREINOC
    // =========================================================================
    private Exercicio exercicioSubstituto; // Mapeia a FK exercicio_substituto_id
    private String seriesPrescritas;       // Retém as séries do template em tempo de execução
    private String repeticoesPrescritas;   // Retém as repetições do template em tempo de execução

    // Constructor Padrão
    public Exercicio() {}

    // Getters e Setters Completos
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

    public Exercicio getExercicioSubstituto() { return exercicioSubstituto; }
    public void setExercicioSubstituto(Exercicio exercicioSubstituto) { this.exercicioSubstituto = exercicioSubstituto; }

    public String getSeriesPrescritas() { return seriesPrescritas; }
    public void setSeriesPrescritas(String seriesPrescritas) { this.seriesPrescritas = seriesPrescritas; }

    public String getRepeticoesPrescritas() { return repeticoesPrescritas; }
    public void setRepeticoesPrescritas(String repeticoesPrescritas) { this.repeticoesPrescritas = repeticoesPrescritas; }
}