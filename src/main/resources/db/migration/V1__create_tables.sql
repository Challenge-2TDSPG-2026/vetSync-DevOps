-- V1: schema completo do dominio VetSync (tutor, pet, especie, raca,
-- clinica, veterinario, tipo de evento, evento de saude, medicamento,
-- prescricao e log de erros).
--
-- Diferenca em relacao ao script original: TB_TUTOR ganhou ds_senha, e
-- TB_VETERINARIO ganhou ds_email e ds_senha, para que os dois perfis
-- consigam ter login proprio no sistema.

CREATE TABLE TB_LOG_ERROS (
                              id_log         INT    IDENTITY(1,1) PRIMARY KEY,
                              nm_procedure   VARCHAR(100),
                              nm_usuario     VARCHAR(100) DEFAULT CURRENT_USER,
                              dt_ocorrencia  DATETIME2     DEFAULT SYSDATETIME(),
                              nr_codigo_erro INT,
                              ds_mensagem    VARCHAR(500)
);

CREATE TABLE TB_TUTOR (
                          id_tutor    INT    IDENTITY(1,1) PRIMARY KEY,
                          nm_tutor    VARCHAR(100) NOT NULL,
                          ds_email    VARCHAR(150) NOT NULL UNIQUE,
                          nr_telefone VARCHAR(20),
                          ds_cpf      CHAR(11)      NOT NULL UNIQUE,
                          ds_senha    VARCHAR(255) NOT NULL,
                          dt_cadastro DATE          DEFAULT GETDATE() NOT NULL
);

CREATE TABLE TB_ESPECIE (
                            id_especie INT    IDENTITY(1,1) PRIMARY KEY,
                            nm_especie VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE TB_RACA (
                         id_raca    INT    IDENTITY(1,1) PRIMARY KEY,
                         nm_raca    VARCHAR(80) NOT NULL,
                         id_especie INT    NOT NULL,
                         CONSTRAINT fk_raca_especie FOREIGN KEY (id_especie) REFERENCES TB_ESPECIE(id_especie)
);

CREATE TABLE TB_PET (
                        id_pet        INT   IDENTITY(1,1) PRIMARY KEY,
                        nm_pet        VARCHAR(80) NOT NULL,
                        dt_nascimento DATE         NOT NULL,
                        ds_sexo       CHAR(1)      CHECK (ds_sexo IN ('M','F')),
                        nr_peso_kg    DECIMAL(5,2),
                        id_tutor      INT   NOT NULL,
                        id_raca       INT    NOT NULL,
                        CONSTRAINT fk_pet_tutor FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
                        CONSTRAINT fk_pet_raca  FOREIGN KEY (id_raca)  REFERENCES TB_RACA(id_raca)
);

CREATE TABLE TB_CLINICA (
                            id_clinica INT    IDENTITY(1,1) PRIMARY KEY,
                            nm_clinica VARCHAR(150) NOT NULL,
                            ds_cnpj    CHAR(14)      NOT NULL UNIQUE,
                            ds_cidade  VARCHAR(80),
                            ds_uf      CHAR(2)
);

CREATE TABLE TB_VETERINARIO (
                                id_veterinario INT    IDENTITY(1,1) PRIMARY KEY,
                                nm_veterinario VARCHAR(100) NOT NULL,
                                nr_crmv        VARCHAR(20)  NOT NULL UNIQUE,
                                ds_email       VARCHAR(150) NOT NULL UNIQUE,
                                ds_senha       VARCHAR(255) NOT NULL,
                                id_clinica     INT    NOT NULL,
                                CONSTRAINT fk_vet_clinica FOREIGN KEY (id_clinica) REFERENCES TB_CLINICA(id_clinica)
);

CREATE TABLE TB_TIPO_EVENTO (
                                id_tipo_evento INT    IDENTITY(1,1) PRIMARY KEY,
                                nm_tipo_evento VARCHAR(80) NOT NULL,
                                ds_categoria   VARCHAR(30) CHECK (ds_categoria IN ('PREVENTIVO','TERAPEUTICO','BEM_ESTAR','EMERGENCIA'))
);

CREATE TABLE TB_EVENTO_SAUDE (
                                 id_evento      INT   IDENTITY(1,1) PRIMARY KEY,
                                 id_pet         INT   NOT NULL,
                                 id_tipo_evento INT    NOT NULL,
                                 id_veterinario INT,
                                 dt_evento      DATE         NOT NULL,
                                 ds_observacao  VARCHAR(500),
                                 vl_custo       DECIMAL(10,2) DEFAULT 0,
                                 CONSTRAINT fk_ev_pet  FOREIGN KEY (id_pet)         REFERENCES TB_PET(id_pet),
                                 CONSTRAINT fk_ev_tipo FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
                                 CONSTRAINT fk_ev_vet  FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario)
);

CREATE TABLE TB_MEDICAMENTO (
                                id_medicamento INT    IDENTITY(1,1) PRIMARY KEY,
                                nm_medicamento VARCHAR(100) NOT NULL,
                                ds_principio   VARCHAR(100),
                                vl_preco_ref   DECIMAL(10,2)
);

CREATE TABLE TB_PRESCRICAO (
                               id_prescricao  INT    IDENTITY(1,1) PRIMARY KEY,
                               id_evento      INT    NOT NULL,
                               id_medicamento INT    NOT NULL,
                               ds_posologia   VARCHAR(200) NOT NULL,
                               dt_inicio      DATE          NOT NULL,
                               dt_fim         DATE,
                               qt_doses_dia   INT,
                               CONSTRAINT fk_presc_evento FOREIGN KEY (id_evento)      REFERENCES TB_EVENTO_SAUDE(id_evento),
                               CONSTRAINT fk_presc_med    FOREIGN KEY (id_medicamento) REFERENCES TB_MEDICAMENTO(id_medicamento)
);
