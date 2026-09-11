package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Raca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RacaRepository extends JpaRepository<Raca, Long> {
    List<Raca> findByEspecie_IdEspecie(Long idEspecie);
    Optional<Raca> findByNmRacaIgnoreCaseAndEspecie_IdEspecie(String nmRaca, Long idEspecie);
}