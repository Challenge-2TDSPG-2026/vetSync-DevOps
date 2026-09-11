package br.com.fiap.VetSync.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "TB_TIPO_EVENTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_evento")
    private Long idTipoEvento;

    @NotBlank(message = "Nome do tipo de evento é obrigatório")
    @Column(name = "nm_tipo_evento", nullable = false, length = 80)
    private String nmTipoEvento;

    @Column(name = "ds_categoria", length = 30)
    private String dsCategoria;
    @Builder.Default
    @Column(name = "nr_pontos", nullable = false)
    private Integer nrPontos = 0;
}