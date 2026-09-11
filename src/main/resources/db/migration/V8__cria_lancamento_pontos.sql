

CREATE TABLE TB_LANCAMENTO_PONTOS (
                                      id_lancamento      INT IDENTITY(1,1) PRIMARY KEY,
                                      id_evento          INT    NOT NULL,
                                      nr_pontos          INT    NOT NULL,
                                      ds_status          VARCHAR(20)  DEFAULT 'PENDENTE' NOT NULL,
                                      dt_lancamento      DATE          DEFAULT GETDATE() NOT NULL,
                                      id_admin_validador INT,
                                      CONSTRAINT uk_lancamento_evento UNIQUE (id_evento),
                                      CONSTRAINT fk_lancamento_evento FOREIGN KEY (id_evento) REFERENCES TB_EVENTO_SAUDE(id_evento),
                                      CONSTRAINT fk_lancamento_admin FOREIGN KEY (id_admin_validador) REFERENCES TB_ADMIN(id_admin),
                                      CONSTRAINT ck_lancamento_status CHECK (ds_status IN ('PENDENTE', 'LIBERADO'))
);

-- Backfill: eventos ja concluidos antes desta migration entram como LIBERADO.
INSERT INTO TB_LANCAMENTO_PONTOS (id_evento, nr_pontos, ds_status, dt_lancamento)
SELECT e.id_evento, ISNULL(t.nr_pontos, 0), 'LIBERADO', GETDATE()
FROM TB_EVENTO_SAUDE e
         JOIN TB_TIPO_EVENTO t ON t.id_tipo_evento = e.id_tipo_evento
WHERE e.ds_status = 'CONCLUIDO';
