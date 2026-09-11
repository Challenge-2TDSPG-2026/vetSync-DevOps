package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByDsEmail(String dsEmail);
}