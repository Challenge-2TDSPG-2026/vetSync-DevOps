package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {
    List<BloqueioAgenda> findByVeterinario_IdVeterinario(Long idVeterinario);
}