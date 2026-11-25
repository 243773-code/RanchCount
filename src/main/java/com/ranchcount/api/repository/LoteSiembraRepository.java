package com.ranchcount.api.repository;

import com.ranchcount.api.entity.EstadoCultivo;
import com.ranchcount.api.entity.LoteSiembra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoteSiembraRepository extends JpaRepository<LoteSiembra, Long> {

    Optional<LoteSiembra> findByCodigoLoteIgnoreCase(String codigoLote);

    boolean existsByCodigoLoteIgnoreCase(String codigoLote);

    List<LoteSiembra> findAllByOrderByFechaSiembraDesc();

    List<LoteSiembra> findByEstadoOrderByFechaSiembraDesc(EstadoCultivo estado);
}
