CREATE TABLE TB_ADMIN (
                          id_admin    INT    IDENTITY(1,1) PRIMARY KEY,
                          nm_admin    VARCHAR(100) NOT NULL,
                          ds_email    VARCHAR(150) NOT NULL UNIQUE,
                          ds_senha    VARCHAR(255) NOT NULL,
                          dt_cadastro DATE          DEFAULT GETDATE() NOT NULL
);