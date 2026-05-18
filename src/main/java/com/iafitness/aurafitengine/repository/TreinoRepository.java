package com.iafitness.aurafitengine.repository;

import com.iafitness.aurafitengine.database.DatabaseConnection;
import com.iafitness.aurafitengine.model.HistoricoCarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TreinoRepository {

    /**
     * Registra uma nova carga realizada pelo usuário no histórico de evolução.
     */
    public boolean registrarCarga(HistoricoCarga historico) {
        String sql = "INSERT INTO historico_cargas (usuario_id, exercicio_id, carga_utilizada, repeticoes_feitas, series_feitas) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, historico.getUsuario().getId());
            stmt.setInt(2, historico.getExercicio().getId());
            stmt.setDouble(3, historico.getCargaUtilizada());
            stmt.setInt(4, historico.getRepeticoesFeitas());
            stmt.setInt(5, historico.getSeriesFeitas());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico de carga: " + e.getMessage());
            return false;
        }
    }
}