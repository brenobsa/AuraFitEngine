package com.iafitness.aurafitengine.repository;

import com.iafitness.aurafitengine.database.DatabaseConnection;
import com.iafitness.aurafitengine.model.DivisaoTreino;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DivisaoTreinoRepository {

    /**
     * Busca todas as divisões de treino configuradas no MySQL para popular a UI de forma dinâmica.
     */
    public List<DivisaoTreino> buscarTodas() {
        List<DivisaoTreino> lista = new ArrayList<>();
        String sql = "SELECT id, nome, frequencia_semanal, descricao FROM divisoes_treino ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DivisaoTreino dt = new DivisaoTreino();
                dt.setId(rs.getInt("id"));
                dt.setNome(rs.getString("nome"));
                dt.setFrequenciaSemanal(rs.getInt("frequencia_semanal"));
                dt.setDescricao(rs.getString("descricao"));
                lista.add(dt);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar divisões de treino: " + e.getMessage());
        }
        return lista;
    }
}