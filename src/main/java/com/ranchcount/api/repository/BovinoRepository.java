package com.ranchcount.api.repository;

import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BovinoRepository extends JpaRepository<Bovino, Long> {

    Optional<Bovino> findByAreteCodigoIgnoreCase(String areteCodigo);

    boolean existsByAreteCodigoIgnoreCase(String areteCodigo);

    List<Bovino> findAllByOrderByIdDesc();

    List<Bovino> findByEstadoOrderByIdDesc(EstadoBovino estado);

    List<Bovino> findBySexoOrderByIdDesc(SexoBovino sexo);

    List<Bovino> findByEstadoAndSexoOrderByIdDesc(EstadoBovino estado, SexoBovino sexo);

    List<Bovino> findByEstadoAndFechaEstimadaPartoBetweenOrderByFechaEstimadaPartoAsc(
            EstadoBovino estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}
