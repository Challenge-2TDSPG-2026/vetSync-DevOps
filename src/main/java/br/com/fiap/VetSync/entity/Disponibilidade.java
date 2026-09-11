package br.com.fiap.VetSync.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_DISPONIBILIDADE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidade")
    private Long idDisponibilidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

    @Column(name = "nr_dia_semana", nullable = false)
    private Integer nrDiaSemana;

    @Column(name = "hr_inicio", nullable = false, length = 5)
    private String hrInicio;

    @Column(name = "hr_fim", nullable = false, length = 5)
    private String hrFim;
}