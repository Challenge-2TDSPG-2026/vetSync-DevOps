package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
}