package com.dzieger.repositories;

import com.dzieger.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Object> findByUsername(String username);
}
