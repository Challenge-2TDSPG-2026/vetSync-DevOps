ALTER TABLE TB_TIPO_EVENTO ADD nr_pontos INT DEFAULT 0 NOT NULL;

CREATE TABLE TB_RECOMPENSA (
                               id_recompensa   INT    IDENTITY(1,1) PRIMARY KEY,
                               nm_recompensa   VARCHAR(150) NOT NULL,
                               ds_descricao    VARCHAR(500),
                               nr_custo_pontos INT    NOT NULL,
                               ds_tipo         VARCHAR(20)  NOT NULL,
                               fl_ativo        INT     DEFAULT 1 NOT NULL,
                               CONSTRAINT ck_recompensa_tipo CHECK (ds_tipo IN ('PRODUTO','CUPOM_DESCONTO')),
                               CONSTRAINT ck_recompensa_custo CHECK (nr_custo_pontos > 0)
);

CREATE TABLE TB_RESGATE (
                            id_resgate                INT  IDENTITY(1,1) PRIMARY KEY,
                            id_tutor                  INT  NOT NULL,
                            id_recompensa              INT  NOT NULL,
                            dt_resgate                 DATETIME2   DEFAULT SYSDATETIME() NOT NULL,
                            id_veterinario_validador   INT,
                            ds_status                  VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
                            CONSTRAINT fk_resgate_tutor FOREIGN KEY (id_tutor) REFERENCES TB_TUTOR(id_tutor),
                            CONSTRAINT fk_resgate_recompensa FOREIGN KEY (id_recompensa) REFERENCES TB_RECOMPENSA(id_recompensa),
                            CONSTRAINT fk_resgate_vet FOREIGN KEY (id_veterinario_validador) REFERENCES TB_VETERINARIO(id_veterinario),
                            CONSTRAINT ck_resgate_status CHECK (ds_status IN ('PENDENTE','VALIDADO','NEGADO'))
);
