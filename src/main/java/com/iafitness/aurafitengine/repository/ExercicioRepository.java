package com.iafitness.aurafitengine.repository;

import com.iafitness.aurafitengine.database.DatabaseConnection;
import com.iafitness.aurafitengine.model.Exercicio;
import com.iafitness.aurafitengine.model.GrupoMuscular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExercicioRepository {

    /**
     * Busca os exercícios de um grupo específico limitando a quantidade de forma aleatória (RAND).
     * Garante o preenchimento do ID do grupo muscular para o mapeador do Java.
     */
    public List<Exercicio> buscarPorGrupoComLimite(String nomeGrupo, int limite) {
        List<Exercicio> exercicios = new ArrayList<>();
        if (limite <= 0) return exercicios;

        String sql = "SELECT e.*, g.nome AS grupo_nome FROM exercicios e " +
                "JOIN grupos_musculares g ON e.grupo_muscular_id = g.id " +
                "WHERE g.nome = ? ORDER BY RAND() LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeGrupo);
            stmt.setInt(2, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exercicio ex = new Exercicio();
                    ex.setId(rs.getInt("id"));
                    ex.setNome(rs.getString("nome"));
                    ex.setFocoAnatomico(rs.getString("foco_anatomico"));
                    ex.setDificuldade(rs.getString("dificuldade"));
                    ex.setTipo(rs.getString("tipo"));

                    // Aloca tanto o ID quanto o Nome resgatados do MySQL
                    GrupoMuscular gm = new GrupoMuscular(rs.getInt("grupo_muscular_id"), rs.getString("grupo_nome"));
                    ex.setGrupoMuscular(gm);

                    exercicios.add(ex);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar exercícios com limite relacional: " + e.getMessage());
        }
        return exercicios;
    }

    /**
     * Busca um único exercício mapeando pelo nome exato para a validação pós-JSON do Ollama.
     */
    public Exercicio buscarPorNome(String nomeExercicio) {
        String sql = "SELECT e.*, g.nome AS grupo_nome FROM exercicios e " +
                "JOIN grupos_musculares g ON e.grupo_muscular_id = g.id " +
                "WHERE e.nome = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeExercicio);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Exercicio ex = new Exercicio();
                    ex.setId(rs.getInt("id"));
                    ex.setNome(rs.getString("nome"));
                    ex.setFocoAnatomico(rs.getString("foco_anatomico"));
                    ex.setDificuldade(rs.getString("dificuldade"));
                    ex.setTipo(rs.getString("tipo"));

                    GrupoMuscular gm = new GrupoMuscular(rs.getInt("grupo_muscular_id"), rs.getString("grupo_nome"));
                    ex.setGrupoMuscular(gm);

                    return ex;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro crítico ao buscar exercício pelo mapeamento nominal: " + e.getMessage());
        }
        return null;
    }
}