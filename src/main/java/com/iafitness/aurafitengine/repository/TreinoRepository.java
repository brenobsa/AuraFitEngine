package com.iafitness.aurafitengine.repository;

import com.iafitness.aurafitengine.model.Exercicio;
import com.iafitness.aurafitengine.model.GrupoMuscular;
import com.iafitness.aurafitengine.model.TemplateItemTreino;
import com.iafitness.aurafitengine.model.TemplatePeriodizacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreinoRepository {

    // CORREÇÃO: Alterado de 3606 para 3306 e adicionadas as flags de compatibilidade do MySQL 8+
    private static final String URL = "jdbc:mysql://localhost:3306/ia_fitness?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Certifique-se de que a senha do seu MySQL local é 'password' mesmo

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Busca o esqueleto completo do treino a partir do template salvo no banco.
     */
    public TemplatePeriodizacao buscarTemplatePorDiretrizes(String divisao, String estimulo) {
        TemplatePeriodizacao template = null;

        String sqlMacro = "SELECT id, nome_divisao, tipo_estimulo, tempo_descanso_s " +
                "FROM templates_periodizacao WHERE nome_divisao = ? AND tipo_estimulo = ?";

        String sqlItens = "SELECT tit.ficha_letra, tit.series_sugeridas, tit.repeticoes_sugeridas, " +
                "ex.id AS ex_id, ex.nome AS ex_nome, ex.foco_anatomico, ex.descricao, ex.execucao, " +
                "gm.nome AS gm_nome, " +
                "sub.id AS sub_id, sub.nome AS sub_nome, sub.descricao AS sub_desc " +
                "FROM template_itens_treino tit " +
                "INNER JOIN exercicios ex ON tit.exercicio_id = ex.id " +
                "INNER JOIN grupos_musculares gm ON ex.grupo_muscular_id = gm.id " +
                "LEFT JOIN exercicios sub ON ex.exercicio_substituto_id = sub.id " +
                "WHERE tit.template_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmtMacro = conn.prepareStatement(sqlMacro)) {

            stmtMacro.setString(1, divisao);
            stmtMacro.setString(2, estimulo);

            try (ResultSet rsMacro = stmtMacro.executeQuery()) {
                if (rsMacro.next()) {
                    template = new TemplatePeriodizacao();
                    template.setId(rsMacro.getInt("id"));
                    template.setNomeDivisao(rsMacro.getString("nome_divisao"));
                    template.setTipoEstimulo(rsMacro.getString("tipo_estimulo"));
                    template.setTempoDescansoS(rsMacro.getInt("tempo_descanso_s"));

                    List<TemplateItemTreino> itens = new ArrayList<>();

                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItens)) {
                        stmtItens.setInt(1, template.getId());
                        try (ResultSet rsItens = stmtItens.executeQuery()) {
                            while (rsItens.next()) {
                                TemplateItemTreino item = new TemplateItemTreino();
                                item.setTemplateId(template.getId());
                                item.setFichaLetra(rsItens.getString("ficha_letra"));
                                item.setSeriesSugeridas(rsItens.getInt("series_sugeridas"));
                                item.setRepeticoesSugeridas(rsItens.getString("repeticoes_sugeridas"));

                                // Monta o exercício principal
                                Exercicio ex = new Exercicio();
                                ex.setId(rsItens.getInt("ex_id"));
                                ex.setNome(rsItens.getString("ex_nome"));
                                ex.setFocoAnatomico(rsItens.getString("foco_anatomico"));
                                ex.setDescricao(rsItens.getString("descricao"));
                                ex.setExecucao(rsItens.getString("execucao"));

                                GrupoMuscular gm = new GrupoMuscular();
                                gm.setNome(rsItens.getString("gm_nome"));
                                ex.setGrupoMuscular(gm);

                                // Injeta os dados de series/reps direto no objeto de treino
                                ex.setSeriesPrescritas(String.valueOf(item.getSeriesSugeridas()));
                                ex.setRepeticoesPrescritas(item.getRepeticoesSugeridas());

                                // Monta o exercício substituto se ele existir (Auto-relacionamento)
                                int subId = rsItens.getInt("sub_id");
                                if (!rsItens.wasNull()) {
                                    Exercicio sub = new Exercicio();
                                    sub.setId(subId);
                                    sub.setNome(rsItens.getString("sub_nome"));
                                    sub.setDescricao(rsItens.getString("sub_desc"));
                                    ex.setExercicioSubstituto(sub);
                                }

                                item.setExercicio(ex);
                                itens.add(item);
                            }
                        }
                    }
                    template.setItens(itens);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return template;
    }
}