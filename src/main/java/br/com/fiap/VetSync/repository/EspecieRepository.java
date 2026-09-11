package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Especie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspecieRepository extends JpaRepository<Especie, Long> {
    Optional<Especie> findByNmEspecieIgnoreCase(String nmEspecie);
}