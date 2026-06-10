-- =========================================================================
-- BANCO DE DADOS - IA FITNESS (SISTEMA INTEGRADO COM MOTOR DE PERIODIZAÇÃO)
-- MOTOR: MYSQL 8.0+
-- REFORMULADO COM O MAPEAMENTO EXATO DE "CAMPEÕES DE USO" E CALIBRAÇÕES
-- =========================================================================

CREATE DATABASE IF NOT EXISTS ia_fitness;
USE ia_fitness;

-- Limpeza de segurança para reexecução livre de conflitos
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS template_itens_treino;
DROP TABLE IF EXISTS templates_periodizacao;
DROP TABLE IF EXISTS historico_cargas;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS exercicios;
DROP TABLE IF EXISTS grupos_musculares;
DROP TABLE IF EXISTS divisoes_treino;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- 0. TABELA DE DIVISÕES DE TREINO (Exigida por DivisaoTreinoRepository)
-- =========================================================================
USE ia_fitness;

CREATE TABLE IF NOT EXISTS historico_cargas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    exercicio_id INT NOT NULL,
    data_registro DATETIME NOT NULL,
    carga_utilizada DOUBLE NOT NULL,
    repeticoes_feitas INT NOT NULL,
    series_feitas INT NOT NULL
);

CREATE TABLE divisoes_treino (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE,
    frequencia_semanal INT NOT NULL,
    descricao TEXT
);

INSERT INTO divisoes_treino (nome, frequencia_semanal, descricao) VALUES
('AB (Superior/Inferior)', 4, 'A divisão coringa das academias para quem treina 4x na semana.'),
('ABC (Sinergia)', 3, 'O queridinho dos marombeiros, ideal para quem vai de 3 a 6 vezes por semana.'),
('ABCD (Volume Localizado)', 4, 'A famosa divisão de quem quer treinar pesado um grupo muscular por dia.'),
('FULLBODY', 3, 'Os exercícios mais clássicos que ativam grandes cadeias musculares de uma só vez.'),
('ABAB (Ondulatória)', 4, 'Excelente para quem quer treinar o mesmo músculo duas vezes na semana com estímulos diferentes.');

-- =========================================================================
-- 1. TABELA DE GRUPOS MUSCULARES
-- =========================================================================
CREATE TABLE grupos_musculares (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO grupos_musculares (nome) VALUES
('Peito'), ('Costas'), ('Quadríceps'), ('Posterior de Coxa'), 
('Ombros'), ('Bíceps'), ('Tríceps'), ('Panturrilhas'), ('Core');

-- =========================================================================
-- 2. TABELA DE EXERCÍCIOS (Mapeamento nominal exato com base nos campeões)
-- =========================================================================
CREATE TABLE exercicios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(200) NOT NULL UNIQUE,
    grupo_muscular_id INT NOT NULL,
    foco_anatomico VARCHAR(100) NOT NULL, 
    dificuldade ENUM('Iniciante', 'Intermediario', 'Avancado') DEFAULT 'Iniciante',
    tipo ENUM('Composto', 'Isolado') DEFAULT 'Composto',
    descricao TEXT,
    execucao TEXT,
    exercicio_substituto_id INT NULL,
    FOREIGN KEY (grupo_muscular_id) REFERENCES grupos_musculares(id),
    FOREIGN KEY (exercicio_substituto_id) REFERENCES exercicios(id)
);

INSERT INTO exercicios (id, nome, grupo_muscular_id, foco_anatomico, dificuldade, tipo, descricao, execucao) VALUES
-- Peito
(1, 'Supino Reto com Barra', 1, 'Geral', 'Iniciante', 'Composto', 'Supino clássico com barra.', 'Descer a barra até o peito.'),
(2, 'Supino Inclinado com Halteres', 1, 'Superior', 'Intermediario', 'Composto', 'Foco na porção superior.', 'Empurrar halteres inclinados.'),
(3, 'Peck Deck (Voador)', 1, 'Geral', 'Iniciante', 'Isolado', 'Isolamento de peitoral.', 'Fechar os braços na máquina.'),
(4, 'Supino Reto com Halteres', 1, 'Geral', 'Iniciante', 'Composto', 'Estabilidade unilateral.', 'Empurrar halteres deitado.'),
(5, 'Supino Reto na Máquina', 1, 'Geral', 'Iniciante', 'Composto', 'Estabilidade mecânica.', 'Empurrar as manoplas para frente.'),
-- Costas
(6, 'Puxada Alta na Polia', 2, 'Largura', 'Iniciante', 'Composto', 'Construção de largura.', 'Puxar a barra até o peito.'),
(7, 'Remada Baixa na Polia', 2, 'Geral', 'Iniciante', 'Composto', 'Espessura geral.', 'Puxar o cabo até o abdômen.'),
(8, 'Remada Baixa Triângulo', 2, 'Espessura', 'Iniciante', 'Composto', 'Foco no miolo das costas.', 'Puxar o triângulo na polia.'),
(9, 'Pulldown na Polia', 2, 'Isolado Latíssimo', 'Intermediario', 'Isolado', 'Isolamento de costas.', 'Puxar com os braços estendidos.'),
(10, 'Remada Articulada na Máquina', 2, 'Espessura', 'Iniciante', 'Composto', 'Máquina articulada.', 'Puxar as manoplas com apoio de peito.'),
(11, 'Extensão Lombar no Banco', 2, 'Lombar', 'Iniciante', 'Isolado', 'Fortalecimento lombar.', 'Realizar a hiperextensão do tronco.'),
-- Pernas (Quadríceps e Posteriores)
(12, 'Leg Press 45°', 3, 'Geral Coxas', 'Iniciante', 'Composto', 'Trabalho pesado guiado.', 'Empurrar a plataforma móvel.'),
(13, 'Cadeira Extensora', 3, 'Isolado Quadríceps', 'Iniciante', 'Isolado', 'Isolamento de anterior.', 'Estender os joelhos completamente.'),
(14, 'Agachamento Livre', 3, 'Geral Pernas', 'Intermediario', 'Composto', 'Exercício livre principal.', 'Agachar mantendo a coluna neutra.'),
(15, 'Agachamento no Smith', 3, 'Geral Pernas', 'Iniciante', 'Composto', 'Agachamento em barra guiada.', 'Agachar mantendo os pés à frente.'),
(16, 'Avanço/Passada com Halteres', 3, 'Unilateral', 'Iniciante', 'Composto', 'Dinâmico unilateral.', 'Caminhar flexionando os joelhos.'),
(17, 'Cadeira Flexora', 4, 'Isolado Posterior', 'Iniciante', 'Isolado', 'Posterior sentado.', 'Flexionar joelhos para baixo.'),
-- Ombros
(18, 'Elevação Lateral com Halteres', 5, 'Lateral', 'Iniciante', 'Isolado', 'Foco no deltoide lateral.', 'Elevar braços lateralmente.'),
(19, 'Desenvolvimento com Halteres', 5, 'Anterior', 'Intermediario', 'Composto', 'Ombros geral.', 'Empurrar halteres verticalmente.'),
(20, 'Crucifixo Inverso na Máquina', 5, 'Posterior', 'Iniciante', 'Isolado', 'Peck deck invertido.', 'Abrir braços para trás na máquina.'),
(21, 'Elevação Lateral', 5, 'Lateral', 'Iniciante', 'Isolado', 'Polia ou máquina.', 'Elevar braço lateralmente.'),
(22, 'Crucifixo Inverso com Halteres', 5, 'Posterior', 'Iniciante', 'Isolado', 'Posterior livre.', 'Inclinar tronco e abrir halteres.'),
-- Braços (Bíceps e Tríceps)
(23, 'Rosca Direta com Halteres', 6, 'Geral Bíceps', 'Iniciante', 'Isolado', 'Bíceps livre com giro.', 'Flexionar cotovelos girando punho.'),
(24, 'Rosca Direta na Barra W', 6, 'Geral Bíceps', 'Iniciante', 'Isolado', 'Pegada anatômica.', 'Flexionar cotovelos com a barra W.'),
(25, 'Rosca Martelo com Halteres', 6, 'Braquial', 'Iniciante', 'Isolado', 'Pegada neutra.', 'Rosca mantendo os polegares para cima.'),
(26, 'Rosca Martelo', 6, 'Braquial', 'Iniciante', 'Isolado', 'Foco em braquiorradial.', 'Rosca com pegada neutra.'),
(27, 'Rosca Concentrada', 6, 'Isolado', 'Iniciante', 'Isolado', 'Pico de bíceps.', 'Apoiado na coxa flexionar o braço.'),
(28, 'Tríceps Pulley (Corda ou Barra)', 7, 'Lateral', 'Iniciante', 'Isolado', 'Tríceps na polia alta.', 'Estender cotovelos para baixo.'),
(29, 'Tríceps Pulley (Barra)', 7, 'Lateral', 'Iniciante', 'Isolado', 'Pegada fixa rígida.', 'Empurrar barra reta para baixo.'),
(30, 'Tríceps Corda', 7, 'Lateral', 'Iniciante', 'Isolado', 'Abertura de corda.', 'Empurrar abrindo mãos no final.'),
-- Panturrilhas e Core
(31, 'Panturrilha em Pé na Máquina', 8, 'Gastrocnêmio', 'Iniciante', 'Isolado', 'Em pé.', 'Elevar calcanhares na máquina.'),
(32, 'Panturrilha em Pé', 8, 'Geral', 'Iniciante', 'Isolado', 'Livre ou degrau.', 'Elevar calcanhares.'),
(33, 'Panturrilha Sentado (Cavalinho)', 8, 'Sóleo', 'Iniciante', 'Isolado', 'Sentado na máquina.', 'Elevar calcanhares sentado.'),
(34, 'Abdominal Clássico no Colchonete', 9, 'Reto Abdominal', 'Iniciante', 'Isolado', 'Tradicional.', 'Flexão de tronco no colchonete.'),
(35, 'Abdominal Clássico', 9, 'Reto Abdominal', 'Iniciante', 'Isolado', 'Livre.', 'Flexão tradicional.'),
(36, 'Abdominal Prancha', 9, 'Isométrico', 'Iniciante', 'Isolado', 'Estabilização.', 'Manter posição estável de prancha.'),
(37, 'Abdominal na Polia', 9, 'Reto com Carga', 'Iniciante', 'Isolado', 'Cabo.', 'Flexão de tronco ajoelhado na polia.'),
(38, 'Abdominal Infra no Colchonete', 9, 'Infra', 'Iniciante', 'Isolado', 'Porção inferior.', 'Elevação de pernas deitado.');

-- Vinculações automáticas antialucinação
UPDATE exercicios SET exercicio_substituto_id = 5 WHERE id = 1; -- Supino Barra -> Supino Máquina
UPDATE exercicios SET exercicio_substituto_id = 12 WHERE id = 14; -- Agachamento Livre -> Leg 45

-- =========================================================================
-- 3. TABELA DE USUÁRIOS E HISTÓRICO DE PROGRESSÃO
-- =========================================================================
CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO usuarios (id, nome, email) VALUES (1, 'Breno Corredor', 'breno@aurafit.com');

-- =========================================================================
-- 4. MATRIZ DE CONFIGURAÇÃO DE TEMPLATES (5 DIVISÕES x 4 FOCOS = 20 MATRIZES)
-- CALIBRANDO OS TEMPOS DE DESCANSO SEGUNDO O NOVO MODELO INFORMADO
-- =========================================================================
CREATE TABLE templates_periodizacao (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome_divisao VARCHAR(50) NOT NULL,    
    tipo_estimulo VARCHAR(50) NOT NULL,   
    tempo_descanso_s INT NOT NULL         
);

INSERT INTO templates_periodizacao (id, nome_divisao, tipo_estimulo, tempo_descanso_s) VALUES
-- FULLBODY (Descansos: Hipertrofia 90s, Força 180s, Resistência 45s, Emagrecimento 45s)
(1, 'FULLBODY', 'Hipertrofia', 90), (2, 'FULLBODY', 'Força', 180), (3, 'FULLBODY', 'Resistência', 45), (4, 'FULLBODY', 'Emagrecimento', 45),
-- AB (Descansos: Hipertrofia 90s, Força 180s, Resistência 45s, Emagrecimento 45s)
(5, 'AB (Superior/Inferior)', 'Hipertrofia', 90), (6, 'AB (Superior/Inferior)', 'Força', 180), (7, 'AB (Superior/Inferior)', 'Resistência', 45), (8, 'AB (Superior/Inferior)', 'Emagrecimento', 45),
-- ABC (Descansos: Hipertrofia 90s, Força 180s, Resistência 45s, Emagrecimento 45s)
(9, 'ABC (Sinergia)', 'Hipertrofia', 90), (10, 'ABC (Sinergia)', 'Força', 180), (11, 'ABC (Sinergia)', 'Resistência', 45), (12, 'ABC (Sinergia)', 'Emagrecimento', 45),
-- ABAB (Descansos: Hipertrofia 90s, Força 180s, Resistência 30s, Emagrecimento 45s)
(13, 'ABAB (Ondulatória)', 'Hipertrofia', 90), (14, 'ABAB (Ondulatória)', 'Força', 180), (15, 'ABAB (Ondulatória)', 'Resistência', 30), (16, 'ABAB (Ondulatória)', 'Emagrecimento', 45),
-- ABCD (Descansos: Hipertrofia 90s, Força 180s, Resistência 45s, Emagrecimento 45s)
(17, 'ABCD (Volume Localizado)', 'Hipertrofia', 90), (18, 'ABCD (Volume Localizado)', 'Força', 180), (19, 'ABCD (Volume Localizado)', 'Resistência', 45), (20, 'ABCD (Volume Localizado)', 'Emagrecimento', 45);

-- =========================================================================
-- 5. POPULANDO AS SÉRIES E REPETIÇÕES DOS TEMPLATES BASEADO NAS CALIBRAÇÕES
-- =========================================================================
CREATE TABLE template_itens_treino (
    id INT PRIMARY KEY AUTO_INCREMENT,
    template_id INT NOT NULL,
    exercicio_id INT NOT NULL,
    ficha_letra CHAR(1) NOT NULL,          
    series_sugeridas INT NOT NULL,
    repeticoes_sugeridas VARCHAR(30) NOT NULL,
    FOREIGN KEY (template_id) REFERENCES templates_periodizacao(id),
    FOREIGN KEY (exercicio_id) REFERENCES exercicios(id)
);

INSERT INTO template_itens_treino (template_id, exercicio_id, ficha_letra, series_sugeridas, repeticoes_sugeridas) VALUES
-- =========================================================================
-- DIVISÃO 1: FULLBODY (Templates 1, 2, 3, 4)
-- =========================================================================
-- Hipertrofia: 3-4 séries de 10-12 reps
(1, 12, 'A', 4, '10-12'), (1, 5, 'A', 4, '10-12'), (1, 6, 'A', 4, '10-12'), (1, 18, 'A', 3, '10-12'), (1, 17, 'A', 3, '10-12'), (1, 34, 'A', 3, '10-12'),
-- Força: 5 séries de 5 reps nos 3 primeiros, demais padrão
(2, 12, 'A', 5, '5'), (2, 5, 'A', 5, '5'), (2, 6, 'A', 5, '5'), (2, 18, 'A', 3, '8'), (2, 17, 'A', 3, '8'), (2, 34, 'A', 3, '10'),
-- Resistência: 3 séries de 15-20 reps
(3, 12, 'A', 3, '15-20'), (3, 5, 'A', 3, '15-20'), (3, 6, 'A', 3, '15-20'), (3, 18, 'A', 3, '15-20'), (3, 17, 'A', 3, '15-20'), (3, 34, 'A', 3, '15-20'),
-- Emagrecimento: 3 séries de 12 reps (Programado como Bi-set no prompt)
(4, 12, 'A', 3, '12'), (4, 5, 'A', 3, '12'), (4, 6, 'A', 3, '12'), (4, 18, 'A', 3, '12'), (4, 17, 'A', 3, '12'), (4, 34, 'A', 3, '12'),

-- =========================================================================
-- DIVISÃO 2: AB (Superior / Inferior - Templates 5, 6, 7, 8)
-- =========================================================================
-- Hipertrofia: 3-4 séries de 8-12 reps
(5, 4, 'A', 4, '8-12'), (5, 6, 'A', 4, '8-12'), (5, 3, 'A', 3, '8-12'), (5, 7, 'A', 3, '8-12'), (5, 23, 'A', 3, '8-12'), (5, 28, 'A', 3, '8-12'),
(5, 14, 'B', 4, '8-12'), (5, 12, 'B', 4, '8-12'), (5, 13, 'B', 3, '8-12'), (5, 17, 'B', 3, '8-12'), (5, 31, 'B', 4, '12-15'), (5, 36, 'B', 3, 'Isometria'),
-- Força: 4 séries de 6 reps nos principais (Supino Halter e Agachamento), demais 3x8
(6, 4, 'A', 4, '6'), (6, 6, 'A', 3, '8'), (6, 3, 'A', 3, '8'), (6, 7, 'A', 3, '8'), (6, 23, 'A', 3, '8'), (6, 28, 'A', 3, '8'),
(6, 14, 'B', 4, '6'), (6, 12, 'B', 3, '8'), (6, 13, 'B', 3, '8'), (6, 17, 'B', 3, '8'), (6, 31, 'B', 3, '8'), (6, 36, 'B', 3, 'Isometria'),
-- Resistência: 3 séries de 15 reps
(7, 4, 'A', 3, '15'), (7, 6, 'A', 3, '15'), (7, 3, 'A', 3, '15'), (7, 7, 'A', 3, '15'), (7, 23, 'A', 3, '15'), (7, 28, 'A', 3, '15'),
(7, 14, 'B', 3, '15'), (7, 12, 'B', 3, '15'), (7, 13, 'B', 3, '15'), (7, 17, 'B', 3, '15'), (7, 31, 'B', 3, '15'), (7, 36, 'B', 3, 'Isometria'),
-- Emagrecimento: 4 séries de 12 reps (Formato Super-set)
(8, 4, 'A', 4, '12'), (8, 6, 'A', 4, '12'), (8, 3, 'A', 4, '12'), (8, 7, 'A', 4, '12'), (8, 23, 'A', 3, '12'), (8, 28, 'A', 3, '12'),
(8, 14, 'B', 4, '12'), (8, 12, 'B', 4, '12'), (8, 13, 'B', 3, '12'), (8, 17, 'B', 3, '12'), (8, 31, 'B', 3, '12'), (8, 36, 'B', 3, 'Isometria'),

-- =========================================================================
-- DIVISÃO 3: ABC (Push / Pull / Legs - Templates 9, 10, 11, 12)
-- =========================================================================
-- Hipertrofia: Grandes 4x8-10, Pequenos (Braços/Ombros) 3x10-12
(9, 1, 'A', 4, '8-10'), (9, 2, 'A', 4, '8-10'), (9, 19, 'A', 3, '10-12'), (9, 18, 'A', 3, '10-12'), (9, 29, 'A', 3, '10-12'),
(9, 6, 'B', 4, '8-10'), (9, 8, 'B', 4, '8-10'), (9, 20, 'B', 3, '10-12'), (9, 24, 'B', 3, '10-12'), (9, 25, 'B', 3, '10-12'),
(9, 15, 'C', 4, '8-10'), (9, 12, 'C', 4, '8-10'), (9, 13, 'C', 3, '10-12'), (9, 17, 'C', 3, '10-12'), (9, 33, 'C', 4, '12-15'),
-- Força: 5 séries de 5 reps no primeiro de cada dia, demais auxiliares estáveis
(10, 1, 'A', 5, '5'), (10, 2, 'A', 3, '6'), (10, 19, 'A', 3, '6'), (10, 18, 'A', 3, '8'), (10, 29, 'A', 3, '8'),
(10, 6, 'B', 5, '5'), (10, 8, 'B', 3, '6'), (10, 20, 'B', 3, '8'), (10, 24, 'B', 3, '6'), (10, 25, 'B', 3, '6'),
(10, 15, 'C', 5, '5'), (10, 12, 'C', 3, '6'), (10, 13, 'C', 3, '6'), (10, 17, 'C', 3, '6'), (10, 33, 'C', 3, '8'),
-- Resistência: 3 séries de 20 reps
(11, 1, 'A', 3, '20'), (11, 2, 'A', 3, '20'), (11, 19, 'A', 3, '20'), (11, 18, 'A', 3, '20'), (11, 29, 'A', 3, '20'),
(11, 6, 'B', 3, '20'), (11, 8, 'B', 3, '20'), (11, 20, 'B', 3, '20'), (11, 24, 'B', 3, '20'), (11, 25, 'B', 3, '20'),
(11, 15, 'C', 3, '20'), (11, 12, 'C', 3, '20'), (11, 13, 'C', 3, '20'), (11, 17, 'C', 3, '20'), (11, 33, 'C', 3, '20'),
-- Emagrecimento: 3 séries de 12-15 reps (+ Intervalo cardio)
(12, 1, 'A', 3, '12-15'), (12, 2, 'A', 3, '12-15'), (12, 19, 'A', 3, '12-15'), (12, 18, 'A', 3, '12-15'), (12, 29, 'A', 3, '12-15'),
(12, 6, 'B', 3, '12-15'), (12, 8, 'B', 3, '12-15'), (12, 20, 'B', 3, '12-15'), (12, 24, 'B', 3, '12-15'), (12, 25, 'B', 3, '12-15'),
(12, 15, 'C', 3, '12-15'), (12, 12, 'C', 3, '12-15'), (12, 13, 'C', 3, '12-15'), (12, 17, 'C', 3, '12-15'), (12, 33, 'C', 3, '12-15'),

-- =========================================================================
-- DIVISÃO 4: ABAB (Híbrido Clássico - Templates 13, 14, 15, 16)
-- =========================================================================
-- Hipertrofia: 3-4 séries de 10 reps
(13, 1, 'A', 4, '10'), (13, 12, 'A', 4, '10'), (13, 3, 'A', 4, '10'), (13, 13, 'A', 4, '10'), (13, 30, 'A', 4, '10'),
(13, 6, 'B', 4, '10'), (13, 17, 'B', 4, '10'), (13, 8, 'B', 4, '10'), (13, 32, 'B', 4, '10'), (13, 23, 'B', 4, '10'),
(13, 19, 'C', 4, '10'), (13, 15, 'C', 4, '10'), (13, 18, 'C', 4, '10'), (13, 16, 'C', 4, '10'), (13, 29, 'C', 4, '10'),
(13, 10, 'D', 4, '10'), (13, 11, 'D', 4, '10'), (13, 25, 'D', 4, '10'), (13, 37, 'D', 4, '10'), (13, 38, 'D', 4, '10'),
-- Força: A1/B1 Pesado (4x4-6 reps), A2/B2 Volume (3x10 reps)
(14, 1, 'A', 4, '4-6'), (14, 12, 'A', 4, '4-6'), (14, 3, 'A', 3, '8'), (14, 13, 'A', 3, '8'), (14, 30, 'A', 3, '8'),
(14, 6, 'B', 4, '4-6'), (14, 17, 'B', 4, '4-6'), (14, 8, 'B', 3, '8'), (14, 32, 'B', 3, '8'), (14, 23, 'B', 3, '8'),
(14, 19, 'C', 3, '10'), (14, 15, 'C', 3, '10'), (14, 18, 'C', 3, '10'), (14, 16, 'C', 3, '10'), (14, 29, 'C', 3, '10'),
(14, 10, 'D', 3, '10'), (14, 11, 'D', 3, '10'), (14, 25, 'D', 3, '10'), (14, 37, 'D', 3, '10'), (14, 38, 'D', 3, '10'),
-- Resistência: 3 séries de 15-20 reps
(15, 1, 'A', 3, '15-20'), (15, 12, 'A', 3, '15-20'), (15, 3, 'A', 3, '15-20'), (15, 13, 'A', 3, '15-20'), (15, 30, 'A', 3, '15-20'),
(15, 6, 'B', 3, '15-20'), (15, 17, 'B', 3, '15-20'), (15, 8, 'B', 3, '15-20'), (15, 32, 'B', 3, '15-20'), (15, 23, 'B', 3, '15-20'),
(15, 19, 'C', 3, '15-20'), (15, 15, 'C', 3, '15-20'), (15, 18, 'C', 3, '15-20'), (15, 16, 'C', 3, '15-20'), (15, 29, 'C', 3, '15-20'),
(15, 10, 'D', 3, '15-20'), (15, 11, 'D', 3, '15-20'), (15, 25, 'D', 3, '15-20'), (15, 37, 'D', 3, '15-20'), (15, 38, 'D', 3, '15-20'),
-- Emagrecimento: 3 séries de 12 reps (Formato Circuito)
(16, 1, 'A', 3, '12'), (16, 12, 'A', 3, '12'), (16, 3, 'A', 3, '12'), (16, 13, 'A', 3, '12'), (16, 30, 'A', 3, '12'),
(16, 6, 'B', 3, '12'), (16, 17, 'B', 3, '12'), (16, 8, 'B', 3, '12'), (16, 32, 'B', 3, '12'), (16, 23, 'B', 3, '12'),
(16, 19, 'C', 3, '12'), (16, 15, 'C', 3, '12'), (16, 18, 'C', 3, '12'), (16, 16, 'C', 3, '12'), (16, 29, 'C', 3, '12'),
(16, 10, 'D', 3, '12'), (16, 11, 'D', 3, '12'), (16, 25, 'D', 3, '12'), (16, 37, 'D', 3, '12'), (16, 38, 'D', 3, '12'),

-- =========================================================================
-- DIVISÃO 5: ABCD (Músculos Isolados - Templates 17, 18, 19, 20)
-- =========================================================================
-- Hipertrofia: 4 séries de 10-12 reps
(17, 1, 'A', 4, '10-12'), (17, 2, 'A', 4, '10-12'), (17, 3, 'A', 4, '10-12'), (17, 29, 'A', 4, '10-12'), (17, 30, 'A', 4, '10-12'),
(17, 6, 'B', 4, '10-12'), (17, 8, 'B', 4, '10-12'), (17, 9, 'B', 4, '10-12'), (17, 24, 'B', 4, '10-12'), (17, 27, 'B', 4, '10-12'),
(17, 15, 'C', 4, '10-12'), (17, 12, 'C', 4, '10-12'), (17, 13, 'C', 4, '10-12'), (17, 17, 'C', 4, '10-12'), (17, 31, 'C', 4, '10-12'),
(17, 19, 'D', 4, '10-12'), (17, 18, 'D', 4, '10-12'), (17, 22, 'D', 4, '10-12'), (17, 35, 'D', 4, '10-12'), (17, 38, 'D', 4, '10-12'),
-- Força: 5x5 nos primeiros, auxiliares estáveis
(18, 1, 'A', 5, '5'), (18, 2, 'A', 5, '5'), (18, 3, 'A', 3, '8'), (18, 29, 'A', 3, '8'), (18, 30, 'A', 3, '8'),
(18, 6, 'B', 5, '5'), (18, 8, 'B', 3, '6'), (18, 9, 'B', 3, '8'), (18, 24, 'B', 3, '6'), (18, 27, 'B', 3, '6'),
(18, 15, 'C', 5, '5'), (18, 12, 'C', 5, '5'), (18, 13, 'C', 3, '6'), (18, 17, 'C', 3, '6'), (18, 31, 'C', 3, '8'),
(18, 19, 'D', 4, '5'), (18, 18, 'D', 3, '8'), (18, 22, 'D', 3, '8'), (18, 35, 'D', 3, '10'), (18, 38, 'D', 3, '10'),
-- Resistência: 4 séries de 15-20 reps
(19, 1, 'A', 4, '15-20'), (19, 2, 'A', 4, '15-20'), (19, 3, 'A', 4, '15-20'), (19, 29, 'A', 4, '15-20'), (19, 30, 'A', 4, '15-20'),
(19, 6, 'B', 4, '15-20'), (19, 8, 'B', 4, '15-20'), (19, 9, 'B', 4, '15-20'), (19, 24, 'B', 4, '15-20'), (19, 27, 'B', 4, '15-20'),
(19, 15, 'C', 4, '15-20'), (19, 12, 'C', 4, '15-20'), (19, 13, 'C', 4, '15-20'), (19, 17, 'C', 4, '15-20'), (19, 31, 'C', 4, '15-20'),
(19, 19, 'D', 4, '15-20'), (19, 18, 'D', 4, '15-20'), (19, 22, 'D', 4, '15-20'), (19, 35, 'D', 4, '15-20'), (19, 38, 'D', 4, '15-20'),
-- Emagrecimento: 3 séries de 12 reps (Formato Rest-Pause)
(20, 1, 'A', 3, '12'), (20, 2, 'A', 3, '12'), (20, 3, 'A', 3, '12'), (20, 29, 'A', 3, '12'), (20, 30, 'A', 3, '12'),
(20, 6, 'B', 3, '12'), (20, 8, 'B', 3, '12'), (20, 9, 'B', 3, '12'), (20, 24, 'B', 3, '12'), (20, 27, 'B', 3, '12'),
(20, 15, 'C', 3, '12'), (20, 12, 'C', 3, '12'), (20, 13, 'C', 3, '12'), (20, 17, 'C', 3, '12'), (20, 31, 'C', 3, '12'),
(20, 19, 'D', 3, '12'), (20, 18, 'D', 3, '12'), (20, 22, 'D', 3, '12'), (20, 35, 'D', 3, '12'), (20, 38, 'D', 3, '12');

ALTER TABLE historico_cargas ADD COLUMN tipo_treino VARCHAR(50) NOT NULL DEFAULT 'Hipertrofia';