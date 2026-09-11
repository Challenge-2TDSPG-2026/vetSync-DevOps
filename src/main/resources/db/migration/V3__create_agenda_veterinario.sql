CREATE TABLE TB_DISPONIBILIDADE (
                                    id_disponibilidade INT  IDENTITY(1,1) PRIMARY KEY,
                                    id_veterinario     INT  NOT NULL,
                                    nr_dia_semana      INT   NOT NULL,
                                    hr_inicio          VARCHAR(5) NOT NULL,
                                    hr_fim             VARCHAR(5) NOT NULL,
                                    CONSTRAINT fk_disp_vet FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario),
                                    CONSTRAINT ck_disp_dia CHECK (nr_dia_semana BETWEEN 1 AND 7)
);

CREATE TABLE TB_BLOQUEIO_AGENDA (
                                    id_bloqueio     INT IDENTITY(1,1) PRIMARY KEY,
                                    id_veterinario  INT NOT NULL,
                                    dt_inicio       DATE       NOT NULL,
                                    dt_fim          DATE       NOT NULL,
                                    ds_motivo       VARCHAR(200),
                                    CONSTRAINT fk_bloq_vet FOREIGN KEY (id_veterinario) REFERENCES TB_VETERINARIO(id_veterinario)
);