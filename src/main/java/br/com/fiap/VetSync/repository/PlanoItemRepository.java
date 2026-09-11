package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.PlanoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanoItemRepository extends JpaRepository<PlanoItem, Long> {

    Optional<PlanoItem> findByEvento_IdEvento(Long idEvento);

    List<PlanoItem> findByPlano_IdPlanoOrderByNrOrdemAsc(Long idPlano);
}