
UPDATE TB_EVENTO_SAUDE
SET ds_status = 'AGENDADO'
WHERE ds_status IN ('SOLICITADO', 'CONFIRMADO');


ALTER TABLE TB_EVENTO_SAUDE DROP CONSTRAINT ck_evento_status;

ALTER TABLE TB_EVENTO_SAUDE ADD CONSTRAINT ck_evento_status
    CHECK (ds_status IN ('AGENDADO', 'CONCLUIDO', 'CANCELADO'));


DECLARE @default_constraint sysname;
DECLARE @drop_default_sql nvarchar(4000);

SELECT @default_constraint = dc.name
FROM sys.default_constraints dc
         INNER JOIN sys.columns c
                    ON c.default_object_id = dc.object_id
         INNER JOIN sys.tables t
                    ON t.object_id = c.object_id
WHERE t.name = 'TB_EVENTO_SAUDE'
  AND c.name = 'ds_status';

IF @default_constraint IS NOT NULL
    BEGIN
        SET @drop_default_sql = N'ALTER TABLE TB_EVENTO_SAUDE DROP CONSTRAINT '
                                 + QUOTENAME(@default_constraint);
        EXEC sys.sp_executesql @drop_default_sql;
    END;

ALTER TABLE TB_EVENTO_SAUDE ADD CONSTRAINT df_evento_status DEFAULT 'AGENDADO' FOR ds_status;
