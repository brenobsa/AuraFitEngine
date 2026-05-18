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
     * Busca todos os exercícios de um determinado grupo muscular.
     * Útil para injetar no contexto da IA ou listar no JavaFX.
     */
    public List<Exercicio> buscarPorGrupoMuscular(String nomeGrupo) {
        List<Exercicio> exercicios = new ArrayList<>();
        String sql = "SELECT e.*, g.nome AS grupo_nome FROM exercicios e " +
                "JOIN grupos_musculares g ON e.grupo_muscular_id = g.id " +
                "WHERE g.nome = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeGrupo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Exercicio ex = new Exercicio();
                    ex.setId(rs.getInt("id"));
                    ex.setNome(rs.getString("nome"));
                    ex.setFocoAnatomico(rs.getString("foco_anatomico"));
                    ex.setDificuldade(rs.getString("dificuldade"));
                    ex.setTipo(rs.getString("tipo"));
                    ex.setDescricao(rs.getString("descricao"));
                    ex.setExecucao(rs.getString("execucao"));

                    // Monta o relacionamento com o Grupo Muscular
                    GrupoMuscular gm = new GrupoMuscular(rs.getInt("grupo_muscular_id"), rs.getString("grupo_nome"));
                    ex.setGrupoMuscular(gm);

                    exercicios.add(ex);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar exercícios por grupo: " + e.getMessage());
        }
        return exercicios;
    }

    /**
     * NOVO: Verifica se o exercício gerado pela IA já existe pelo nome.
     * Se não existir, faz a inserção e atualiza o objeto com o ID gerado pelo MySQL.
     */
    public void salvarSeNaoExistir(Exercicio exercicio) {
        Exercicio existente = buscarPorNome(exercicio.getNome());

        if (existente != null) {
            // Se já existe no banco, aproveita o ID existente para não duplicar dados
            exercicio.setId(existente.getId());
            return;
        }

        // Caso contrário, insere um novo registro (usando o id=1/Peito como padrão para novos inputs de IA)
        String sql = "INSERT INTO exercicios (nome, grupo_muscular_id, foco_anatomico, tipo, dificuldade, descricao) " +
                "VALUES (?, 1, ?, ?, ?, 'Gerado automaticamente pelo AuraFit Engine')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, exercicio.getNome());
            stmt.setString(2, exercicio.getFocoAnatomico());
            stmt.setString(3, exercicio.getTipo());
            stmt.setString(4, exercicio.getDificuldade());

            stmt.executeUpdate();

            // Recupera o ID gerado automaticamente pelo AUTO_INCREMENT do MySQL
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exercicio.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar exercício automático gerado pela IA: " + e.getMessage());
        }
    }

    /**
     * NOVO: Método auxiliar para buscar um exercício diretamente pelo nome estruturado.
     */
    public Exercicio buscarPorNome(String nomeExercicio) {
        String sql = "SELECT * FROM exercicios WHERE nome = ?";

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
                    return ex;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar exercício por nome: " + e.getMessage());
        }
        return null; // Retorna null se não encontrar correspondência
    }
}