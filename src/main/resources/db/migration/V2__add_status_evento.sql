
ALTER TABLE TB_EVENTO_SAUDE ADD ds_status VARCHAR(20) DEFAULT 'SOLICITADO' NOT NULL;
ALTER TABLE TB_EVENTO_SAUDE ADD ds_motivo_cancelamento VARCHAR(300);

-- O SQL Server compila o batch inteiro antes de executa-lo. A constraint
-- precisa ser criada dinamicamente para que ds_status ja exista quando
-- a expressao CHECK for compilada.
EXEC(N'ALTER TABLE TB_EVENTO_SAUDE ADD CONSTRAINT ck_evento_status
    CHECK (ds_status IN (''SOLICITADO'',''CONFIRMADO'',''CONCLUIDO'',''CANCELADO''))');
