package com.ranchcount.api.repository;

import com.ranchcount.api.entity.TipoTransaccion;
import com.ranchcount.api.entity.Transaccion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    @Override
    @EntityGraph(attributePaths = {"registradoPor", "bovino", "loteSiembra"})
    Optional<Transaccion> findById(Long id);

    @EntityGraph(attributePaths = {"registradoPor", "bovino", "loteSiembra"})
    List<Transaccion> findAllByOrderByFechaTransaccionDescIdDesc();

    @EntityGraph(attributePaths = {"registradoPor", "bovino", "loteSiembra"})
    List<Transaccion> findByTipoOrderByFechaTransaccionDescIdDesc(TipoTransaccion tipo);

    @EntityGraph(attributePaths = {"registradoPor", "bovino", "loteSiembra"})
    List<Transaccion> findByFechaTransaccionBetweenOrderByFechaTransaccionDescIdDesc(
            LocalDate desde,
            LocalDate hasta
    );

    @EntityGraph(attributePaths = {"registradoPor", "bovino", "loteSiembra"})
    List<Transaccion> findByTipoAndFechaTransaccionBetweenOrderByFechaTransaccionDescIdDesc(
            TipoTransaccion tipo,
            LocalDate desde,
            LocalDate hasta
    );

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.tipo = :tipo")
    BigDecimal sumMontoByTipo(@Param("tipo") TipoTransaccion tipo);

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t "
            + "WHERE t.tipo = :tipo AND t.fechaTransaccion BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByTipoAndFechaBetween(
            @Param("tipo") TipoTransaccion tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );
}
