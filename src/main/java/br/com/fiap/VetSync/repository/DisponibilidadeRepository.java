package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, Long> {
    List<Disponibilidade> findByVeterinario_IdVeterinario(Long idVeterinario);
}