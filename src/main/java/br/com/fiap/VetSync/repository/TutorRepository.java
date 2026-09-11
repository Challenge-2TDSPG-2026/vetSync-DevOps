package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByDsEmail(String dsEmail);
    boolean existsByDsEmail(String dsEmail);
}