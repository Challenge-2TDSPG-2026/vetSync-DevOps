package br.com.fiap.VetSync.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "TB_ESPECIE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Especie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Long idEspecie;

    @NotBlank(message = "Nome da espécie é obrigatório")
    @Column(name = "nm_especie", nullable = false, unique = true, length = 50)
    private String nmEspecie;
}