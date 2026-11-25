package com.ranchcount.api.repository;

import com.ranchcount.api.entity.RanchoConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RanchoConfigRepository extends JpaRepository<RanchoConfig, Long> {
    Optional<RanchoConfig> findTopByOrderByIdAsc();
}
