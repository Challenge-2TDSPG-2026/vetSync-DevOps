package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicaRepository extends JpaRepository<Clinica, Long> {
}