-- VetSync - DDL completo para SQL Server / Azure SQL
-- Executar em um banco vazio. Este arquivo representa o estado final do schema
-- depois de todas as migrations V1 a V10.
-- Nao inclui dados de teste ou credenciais.

-- Registro tecnico de erros do banco e da aplicacao.
CREATE TABLE TB_LOG_ERROS (
    id_log         INT IDENTITY(1,1) PRIMARY KEY,
    nm_procedure   VARCHAR(100),
    nm_usuario     VARCHAR(100) DEFAULT CURRENT_USER,
    dt_ocorrencia  DATETIME2 DEFAULT SYSDATETIME(),
    nr_codigo_erro INT,
    ds_mensagem    VARCHAR(500)
);

-- Tutor responsavel pelos pets.
CREATE TABLE TB_TUTOR (
    id_tutor      INT IDENTITY(1,1) PRIMARY KEY,
    nm_tutor      VARCHAR(100) NOT NULL,
    ds_email      VARCHAR(150) NOT NULL UNIQUE,
    nr_telefone   VARCHAR(20),
    ds_cpf        CHAR(11) NOT NULL UNIQUE,
    ds_senha      VARCHAR(255) NOT NULL,
    dt_cadastro   DATE DEFAULT GETDATE() NOT NULL
);

-- Especies disponiveis para classificacao dos pets.
CREATE TABLE TB_ESPECIE (
    id_especie   INT IDENTITY(1,1) PRIMARY KEY,
    nm_especie   VARCHAR(50) NOT NULL UNIQUE
);

-- Racas vinculadas a uma especie.
CREATE TABLE TB_RACA (
    id_raca      INT IDENTITY(1,1) PRIMARY KEY,
    nm_raca      VARCHAR(80) NOT NULL,
    id_especie   INT NOT NULL,
    CONSTRAINT fk_raca_especie
        FOREIGN KEY (id_especie) REFERENCES TB_ESPECIE(id_especie)
);

-- Pet acompanhado pela aplicacao.
CREATE TABLE TB_PET (
    id_pet          INT IDENTITY(1,1) PRIMARY KEY,
    nm_pet          VARCHAR(80) NOT NULL,
    dt_nascimento   DATE NOT NULL,
    ds_sexo         CHAR(1) CHECK (ds_sexo IN ('M', 'F')),
    nr_peso_kg      DECIMAL(5,2),
    id_tutor        INT NOT NULL,
    id_raca         INT NOT NULL,
    CONSTRAINT fk_pet_tutor
        FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
    CONSTRAINT fk_pet_raca
        FOREIGN KEY (id_raca) REFERENCES TB_RACA(id_raca)
);

-- Clinica veterinaria participante da solucao.
CREATE TABLE TB_CLINICA (
    id_clinica   INT IDENTITY(1,1) PRIMARY KEY,
    nm_clinica   VARCHAR(150) NOT NULL,
    ds_cnpj      CHAR(14) NOT NULL UNIQUE,
    ds_cidade    VARCHAR(80),
    ds_uf        CHAR(2)
);

-- Veterinario que atende os pets em uma clinica.
CREATE TABLE TB_VETERINARIO (
    id_veterinario   INT IDENTITY(1,1) PRIMARY KEY,
    nm_veterinario   VARCHAR(100) NOT NULL,
    nr_crmv          VARCHAR(20) NOT NULL UNIQUE,
    ds_email         VARCHAR(150) NOT NULL UNIQUE,
    ds_senha         VARCHAR(255) NOT NULL,
    id_clinica       INT NOT NULL,
    CONSTRAINT fk_vet_clinica
        FOREIGN KEY (id_clinica) REFERENCES TB_CLINICA(id_clinica)
);

-- Administrador responsavel por validacoes internas.
CREATE TABLE TB_ADMIN (
    id_admin       INT IDENTITY(1,1) PRIMARY KEY,
    nm_admin       VARCHAR(100) NOT NULL,
    ds_email       VARCHAR(150) NOT NULL UNIQUE,
    ds_senha       VARCHAR(255) NOT NULL,
    dt_cadastro    DATE DEFAULT GETDATE() NOT NULL
);

-- Tipos de eventos de saude e pontuacao correspondente.
CREATE TABLE TB_TIPO_EVENTO (
    id_tipo_evento   INT IDENTITY(1,1) PRIMARY KEY,
    nm_tipo_evento   VARCHAR(80) NOT NULL,
    ds_categoria     VARCHAR(30) CHECK (ds_categoria IN ('PREVENTIVO', 'TERAPEUTICO', 'BEM_ESTAR', 'EMERGENCIA')),
    nr_pontos        INT DEFAULT 0 NOT NULL
);

-- Eventos de saude relacionados a um pet e, quando aplicavel, a um veterinario.
CREATE TABLE TB_EVENTO_SAUDE (
    id_evento       INT IDENTITY(1,1) PRIMARY KEY,
    id_pet          INT NOT NULL,
    id_tipo_evento  INT NOT NULL,
    id_veterinario  INT,
    dt_evento       DATE NOT NULL,
    hr_evento       VARCHAR(5),
    ds_observacao   VARCHAR(500),
    vl_custo        DECIMAL(10,2) DEFAULT 0,
    ds_status       VARCHAR(20) DEFAULT 'AGENDADO' NOT NULL,
    ds_motivo_cancelamento VARCHAR(300),
    CONSTRAINT fk_ev_pet
        FOREIGN KEY (id_pet) REFERENCES TB_PET(id_pet),
    CONSTRAINT fk_ev_tipo
        FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
    CONSTRAINT fk_ev_vet
        FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
    CONSTRAINT ck_evento_status
        CHECK (ds_status IN ('AGENDADO', 'CONCLUIDO', 'CANCELADO'))
);

-- Catalogo de medicamentos utilizados nas prescricoes.
CREATE TABLE TB_MEDICAMENTO (
    id_medicamento   INT IDENTITY(1,1) PRIMARY KEY,
    nm_medicamento   VARCHAR(100) NOT NULL,
    ds_principio     VARCHAR(100),
    vl_preco_ref     DECIMAL(10,2)
);

-- Prescricao vinculada a um evento e a um medicamento.
CREATE TABLE TB_PRESCRICAO (
    id_prescricao     INT IDENTITY(1,1) PRIMARY KEY,
    id_evento         INT NOT NULL,
    id_medicamento    INT NOT NULL,
    ds_posologia      VARCHAR(200) NOT NULL,
    dt_inicio         DATE NOT NULL,
    dt_fim            DATE,
    qt_doses_dia      INT,
    ds_status         VARCHAR(20) DEFAULT 'SOLICITADO' NOT NULL,
    id_admin_validador INT,
    CONSTRAINT fk_presc_evento
        FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
    CONSTRAINT fk_presc_med
        FOREIGN KEY (id_medicamento) REFERENCES TB_MEDICAMENTO(id_medicamento),
    CONSTRAINT fk_prescricao_admin_validador
        FOREIGN KEY (id_admin_validador) REFERENCES TB_ADMIN(id_admin),
    CONSTRAINT ck_prescricao_status
        CHECK (ds_status IN ('SOLICITADO', 'LIBERADO', 'NEGADO'))
);

-- Horarios recorrentes de disponibilidade do veterinario.
CREATE TABLE TB_DISPONIBILIDADE (
    id_disponibilidade   INT IDENTITY(1,1) PRIMARY KEY,
    id_veterinario       INT NOT NULL,
    nr_dia_semana        INT NOT NULL,
    hr_inicio            VARCHAR(5) NOT NULL,
    hr_fim               VARCHAR(5) NOT NULL,
    CONSTRAINT fk_disp_vet
        FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
    CONSTRAINT ck_disp_dia
        CHECK (nr_dia_semana BETWEEN 1 AND 7)
);

-- Bloqueios pontuais da agenda do veterinario.
CREATE TABLE TB_BLOQUEIO_AGENDA (
    id_bloqueio       INT IDENTITY(1,1) PRIMARY KEY,
    id_veterinario    INT NOT NULL,
    dt_inicio         DATE NOT NULL,
    dt_fim            DATE NOT NULL,
    ds_motivo         VARCHAR(200),
    CONSTRAINT fk_bloq_vet
        FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario)
);

-- Recompensas disponiveis no catalogo de pontos.
CREATE TABLE TB_RECOMPENSA (
    id_recompensa     INT IDENTITY(1,1) PRIMARY KEY,
    nm_recompensa     VARCHAR(150) NOT NULL,
    ds_descricao      VARCHAR(500),
    nr_custo_pontos   INT NOT NULL,
    ds_tipo           VARCHAR(20) NOT NULL,
    fl_ativo          INT DEFAULT 1 NOT NULL,
    CONSTRAINT ck_recompensa_tipo
        CHECK (ds_tipo IN ('PRODUTO', 'CUPOM_DESCONTO')),
    CONSTRAINT ck_recompensa_custo
        CHECK (nr_custo_pontos > 0)
);

-- Resgates de recompensas realizados por tutores.
CREATE TABLE TB_RESGATE (
    id_resgate              INT IDENTITY(1,1) PRIMARY KEY,
    id_tutor                INT NOT NULL,
    id_recompensa           INT NOT NULL,
    dt_resgate              DATETIME2 DEFAULT SYSDATETIME() NOT NULL,
    id_veterinario_validador INT,
    ds_status               VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
    CONSTRAINT fk_resgate_tutor
        FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
    CONSTRAINT fk_resgate_recompensa
        FOREIGN KEY (id_recompensa) REFERENCES TB_RECOMPENSA(id_recompensa),
    CONSTRAINT fk_resgate_vet
        FOREIGN KEY (id_veterinario_validador) REFERENCES TB_VETERINARIO(id_veterinario),
    CONSTRAINT ck_resgate_status
        CHECK (ds_status IN ('PENDENTE', 'VALIDADO', 'NEGADO'))
);

-- Planos de tratamento prescritos para um pet.
CREATE TABLE TB_PLANO_TRATAMENTO (
    id_plano          INT IDENTITY(1,1) PRIMARY KEY,
    id_pet            INT NOT NULL,
    id_veterinario    INT NOT NULL,
    nr_pontos_bonus   INT DEFAULT 0 NOT NULL,
    ds_status         VARCHAR(20) DEFAULT 'EM_ANDAMENTO' NOT NULL,
    dt_criacao        DATE DEFAULT GETDATE() NOT NULL,
    CONSTRAINT fk_plano_pet
        FOREIGN KEY (id_pet) REFERENCES TB_PET(id_pet),
    CONSTRAINT fk_plano_veterinario
        FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
    CONSTRAINT ck_plano_status
        CHECK (ds_status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'QUEBRADO'))
);

-- Itens ordenados de cada plano de tratamento.
CREATE TABLE TB_PLANO_ITEM (
    id_item          INT IDENTITY(1,1) PRIMARY KEY,
    id_plano         INT NOT NULL,
    nr_ordem         INT NOT NULL,
    id_tipo_evento   INT NOT NULL,
    id_evento        INT,
    ds_status        VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
    CONSTRAINT fk_item_plano
        FOREIGN KEY (id_plano) REFERENCES TB_PLANO_TRATAMENTO(id_plano),
    CONSTRAINT fk_item_tipo_evento
        FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
    CONSTRAINT fk_item_evento
        FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
    CONSTRAINT uk_item_plano_ordem
        UNIQUE (id_plano, nr_ordem),
    CONSTRAINT ck_item_status
        CHECK (ds_status IN ('PENDENTE', 'AGENDADO', 'CONCLUIDO', 'QUEBRADO'))
);

CREATE UNIQUE INDEX uk_item_evento
    ON TB_PLANO_ITEM (id_evento)
    WHERE id_evento IS NOT NULL;

-- Lancamentos de pontos originados por eventos ou por planos concluidos.
CREATE TABLE TB_LANCAMENTO_PONTOS (
    id_lancamento        INT IDENTITY(1,1) PRIMARY KEY,
    id_evento            INT,
    nr_pontos            INT NOT NULL,
    ds_status            VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
    dt_lancamento        DATE DEFAULT GETDATE() NOT NULL,
    id_admin_validador   INT,
    id_plano_tratamento  INT,
    CONSTRAINT fk_lancamento_evento
        FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
    CONSTRAINT fk_lancamento_admin
        FOREIGN KEY (id_admin_validador) REFERENCES TB_ADMIN(id_admin),
    CONSTRAINT fk_lancamento_plano
        FOREIGN KEY (id_plano_tratamento) REFERENCES TB_PLANO_TRATAMENTO(id_plano),
    CONSTRAINT ck_lancamento_status
        CHECK (ds_status IN ('PENDENTE', 'LIBERADO')),
    CONSTRAINT ck_lancamento_origem
        CHECK (
            (id_evento IS NOT NULL AND id_plano_tratamento IS NULL)
            OR
            (id_evento IS NULL AND id_plano_tratamento IS NOT NULL)
        )
 );

-- Indices unicos filtrados permitem varias linhas de cada origem sem aceitar
-- duas linhas para o mesmo evento ou para o mesmo plano.
CREATE UNIQUE INDEX uk_lancamento_evento
    ON TB_LANCAMENTO_PONTOS (id_evento)
    WHERE id_evento IS NOT NULL;

CREATE UNIQUE INDEX uk_lancamento_plano
    ON TB_LANCAMENTO_PONTOS (id_plano_tratamento)
    WHERE id_plano_tratamento IS NOT NULL;
