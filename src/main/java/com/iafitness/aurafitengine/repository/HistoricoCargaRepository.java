package com.iafitness.aurafitengine.repository;

import com.iafitness.aurafitengine.model.HistoricoCarga;
import com.iafitness.aurafitengine.model.Exercicio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoCargaRepository {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ia_fitness", "root", "");
    }

    public boolean salvar(HistoricoCarga historico) {
        String sqlCheck = "SELECT id FROM historico_cargas WHERE usuario_id = ? AND exercicio_id = ? AND DATE(data_registro) = DATE(?) AND tipo_treino = ?";
        String sqlInsert = "INSERT INTO historico_cargas (usuario_id, exercicio_id, data_registro, carga_utilizada, repeticoes_feitas, series_feitas, tipo_treino) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdate = "UPDATE historico_cargas SET carga_utilizada = ? WHERE id = ?";

        try (Connection conn = getConnection()) {
            int existenteId = -1;

            try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                stmtCheck.setInt(1, historico.getUsuario().getId());
                stmtCheck.setInt(2, historico.getExercicio().getId());
                stmtCheck.setTimestamp(3, Timestamp.valueOf(historico.getDataRegistro()));
                stmtCheck.setString(4, historico.getTipoTreino());

                try (ResultSet rs = stmtCheck.executeQuery()) {
                    if (rs.next()) {
                        existenteId = rs.getInt("id");
                    }
                }
            }

            if (existenteId != -1) {
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setDouble(1, historico.getCargaUtilizada());
                    stmtUpdate.setInt(2, existenteId);
                    return stmtUpdate.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    stmtInsert.setInt(1, historico.getUsuario().getId());
                    stmtInsert.setInt(2, historico.getExercicio().getId());
                    stmtInsert.setTimestamp(3, Timestamp.valueOf(historico.getDataRegistro()));
                    stmtInsert.setDouble(4, historico.getCargaUtilizada());
                    stmtInsert.setInt(5, historico.getRepeticoesFeitas());
                    stmtInsert.setInt(6, historico.getSeriesFeitas());
                    stmtInsert.setString(7, historico.getTipoTreino());
                    return stmtInsert.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HistoricoCarga> buscarPorUsuario(int usuarioId) {
        List<HistoricoCarga> lista = new ArrayList<>();
        String sql = "SELECT id, usuario_id, exercicio_id, data_registro, carga_utilizada, repeticoes_feitas, series_feitas, tipo_treino " +
                "FROM historico_cargas WHERE usuario_id = ? ORDER BY data_registro ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoricoCarga hc = new HistoricoCarga();
                    hc.setId(rs.getInt("id"));
                    hc.setCargaUtilizada(rs.getDouble("carga_utilizada"));
                    hc.setDataRegistro(rs.getTimestamp("data_registro").toLocalDateTime());
                    hc.setRepeticoesFeitas(rs.getInt("repeticoes_feitas"));
                    hc.setSeriesFeitas(rs.getInt("series_feitas"));
                    hc.setTipoTreino(rs.getString("tipo_treino"));

                    Exercicio ex = new Exercicio();
                    ex.setId(rs.getInt("exercicio_id"));
                    hc.setExercicio(ex);
                    lista.add(hc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}