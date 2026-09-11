

CREATE TABLE TB_PLANO_TRATAMENTO (
                                     id_plano        INT    IDENTITY(1,1) PRIMARY KEY,
                                     id_pet          INT    NOT NULL,
                                     id_veterinario  INT    NOT NULL,
                                     nr_pontos_bonus INT    DEFAULT 0 NOT NULL,
                                     ds_status       VARCHAR(20)  DEFAULT 'EM_ANDAMENTO' NOT NULL,
                                     dt_criacao      DATE          DEFAULT GETDATE() NOT NULL,
                                     CONSTRAINT fk_plano_pet FOREIGN KEY (id_pet) REFERENCES TB_PET(id_pet),
                                     CONSTRAINT fk_plano_veterinario FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
                                     CONSTRAINT ck_plano_status CHECK (ds_status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'QUEBRADO'))
);

CREATE TABLE TB_PLANO_ITEM (
                               id_item        INT   IDENTITY(1,1) PRIMARY KEY,
                               id_plano       INT   NOT NULL,
                               nr_ordem       INT    NOT NULL,
                               id_tipo_evento INT   NOT NULL,
                               id_evento      INT,
                               ds_status      VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
                               CONSTRAINT fk_item_plano FOREIGN KEY (id_plano) REFERENCES TB_PLANO_TRATAMENTO(id_plano),
                               CONSTRAINT fk_item_tipo_evento FOREIGN KEY (id_tipo_evento) REFERENCES TB_TIPO_EVENTO(id_tipo_evento),
                               CONSTRAINT fk_item_evento FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
                               CONSTRAINT uk_item_plano_ordem UNIQUE (id_plano, nr_ordem),
                               CONSTRAINT ck_item_status CHECK (ds_status IN ('PENDENTE', 'AGENDADO', 'CONCLUIDO', 'QUEBRADO'))
);

CREATE UNIQUE INDEX uk_item_evento
    ON TB_PLANO_ITEM (id_evento)
    WHERE id_evento IS NOT NULL;


ALTER TABLE TB_LANCAMENTO_PONTOS ALTER COLUMN id_evento INT NULL;

ALTER TABLE TB_LANCAMENTO_PONTOS DROP CONSTRAINT uk_lancamento_evento;

ALTER TABLE TB_LANCAMENTO_PONTOS ADD id_plano_tratamento INT;

EXEC(N'ALTER TABLE TB_LANCAMENTO_PONTOS ADD CONSTRAINT fk_lancamento_plano
    FOREIGN KEY (id_plano_tratamento) REFERENCES TB_PLANO_TRATAMENTO(id_plano);
');

EXEC(N'CREATE UNIQUE INDEX uk_lancamento_evento
    ON TB_LANCAMENTO_PONTOS (id_evento)
    WHERE id_evento IS NOT NULL');

EXEC(N'CREATE UNIQUE INDEX uk_lancamento_plano
    ON TB_LANCAMENTO_PONTOS (id_plano_tratamento)
    WHERE id_plano_tratamento IS NOT NULL');

EXEC(N'ALTER TABLE TB_LANCAMENTO_PONTOS ADD CONSTRAINT ck_lancamento_origem CHECK (
    (id_evento IS NOT NULL AND id_plano_tratamento IS NULL)
        OR
        (id_evento IS NULL AND id_plano_tratamento IS NOT NULL)
    )');
