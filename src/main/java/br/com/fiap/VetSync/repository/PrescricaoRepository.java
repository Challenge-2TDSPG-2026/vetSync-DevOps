package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Prescricao;
import br.com.fiap.VetSync.entity.StatusPrescricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescricaoRepository extends JpaRepository<Prescricao, Long> {
    List<Prescricao> findByEvento_IdEvento(Long idEvento);
    List<Prescricao> findByEvento_Pet_Tutor_DsEmailOrderByIdPrescricaoDesc(String email);
    List<Prescricao> findByEvento_Veterinario_DsEmailOrderByIdPrescricaoDesc(String email);
    List<Prescricao> findByDsStatusOrderByIdPrescricaoAsc(StatusPrescricao status);
}