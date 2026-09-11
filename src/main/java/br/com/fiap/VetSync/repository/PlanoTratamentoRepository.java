package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.PlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, Long> {
    List<PlanoTratamento> findByPet_Tutor_DsEmailOrderByDtCriacaoDesc(String email);
    List<PlanoTratamento> findByVeterinario_DsEmailOrderByDtCriacaoDesc(String email);
}