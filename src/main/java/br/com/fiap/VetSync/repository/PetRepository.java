package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByTutor_IdTutor(Long idTutor);
}