package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.TransaccionRequest;
import com.ranchcount.api.dto.response.ResumenFinancieroResponse;
import com.ranchcount.api.dto.response.TransaccionResponse;
import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.LoteSiembra;
import com.ranchcount.api.entity.TipoTransaccion;
import com.ranchcount.api.entity.Transaccion;
import com.ranchcount.api.entity.Usuario;
import com.ranchcount.api.exception.BusinessRuleException;
import com.ranchcount.api.exception.ResourceNotFoundException;
import com.ranchcount.api.repository.BovinoRepository;
import com.ranchcount.api.repository.LoteSiembraRepository;
import com.ranchcount.api.repository.TransaccionRepository;
import com.ranchcount.api.repository.UsuarioRepository;
import com.ranchcount.api.service.TransaccionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final BovinoRepository bovinoRepository;
    private final LoteSiembraRepository loteSiembraRepository;

    public TransaccionServiceImpl(
            TransaccionRepository transaccionRepository,
            UsuarioRepository usuarioRepository,
            BovinoRepository bovinoRepository,
            LoteSiembraRepository loteSiembraRepository) {
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.bovinoRepository = bovinoRepository;
        this.loteSiembraRepository = loteSiembraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listar(TipoTransaccion tipo, LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);

        List<Transaccion> transacciones;
        if (desde != null && hasta != null && tipo != null) {
            transacciones = transaccionRepository
                    .findByTipoAndFechaTransaccionBetweenOrderByFechaTransaccionDescIdDesc(tipo, desde, hasta);
        } else if (desde != null && hasta != null) {
            transacciones = transaccionRepository
                    .findByFechaTransaccionBetweenOrderByFechaTransaccionDescIdDesc(desde, hasta);
        } else if (tipo != null) {
            transacciones = transaccionRepository
                    .findByTipoOrderByFechaTransaccionDescIdDesc(tipo);
        } else {
            transacciones = transaccionRepository
                    .findAllByOrderByFechaTransaccionDescIdDesc();
        }

        return transacciones.stream().map(TransaccionResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse obtenerPorId(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaccion", "id", id));
        return TransaccionResponse.fromEntity(transaccion);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenFinancieroResponse obtenerResumen(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);

        BigDecimal ingresos;
        BigDecimal egresos;

        if (desde != null && hasta != null) {
            ingresos = transaccionRepository.sumMontoByTipoAndFechaBetween(TipoTransaccion.INGRESO, desde, hasta);
            egresos = transaccionRepository.sumMontoByTipoAndFechaBetween(TipoTransaccion.EGRESO, desde, hasta);
        } else {
            ingresos = transaccionRepository.sumMontoByTipo(TipoTransaccion.INGRESO);
            egresos = transaccionRepository.sumMontoByTipo(TipoTransaccion.EGRESO);
        }

        BigDecimal totalIngresos = normalizarEscala(ingresos);
        BigDecimal totalEgresos = normalizarEscala(egresos);
        BigDecimal balanceNeto = totalIngresos.subtract(totalEgresos).setScale(2, RoundingMode.HALF_UP);

        return new ResumenFinancieroResponse(totalIngresos, totalEgresos, balanceNeto, desde, hasta);
    }

    @Override
    @Transactional
    public TransaccionResponse crear(TransaccionRequest request, String usernameAutenticado) {
        Usuario autor = usuarioRepository.findByUsernameIgnoreCase(usernameAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", usernameAutenticado));

        Transaccion transaccion = new Transaccion();
        mapearCampos(transaccion, request);
        transaccion.setRegistradoPor(autor);

        return TransaccionResponse.fromEntity(transaccionRepository.save(transaccion));
    }

    @Override
    @Transactional
    public TransaccionResponse actualizar(Long id, TransaccionRequest request) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaccion", "id", id));

        mapearCampos(transaccion, request);
        return TransaccionResponse.fromEntity(transaccionRepository.save(transaccion));
    }

    @Override
    @Transactional
    public void eliminarLogicamente(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaccion", "id", id));
        // Ejecuta automaticamente @SQLDelete -> UPDATE transacciones SET eliminado_at = CURRENT_TIMESTAMP WHERE id = ?
        transaccionRepository.delete(transaccion);
    }

    private void mapearCampos(Transaccion transaccion, TransaccionRequest request) {
        transaccion.setTipo(request.tipo());
        transaccion.setCategoria(request.categoria());
        transaccion.setMonto(normalizarEscala(request.monto()));
        transaccion.setConcepto(request.concepto().trim());
        transaccion.setFechaTransaccion(request.fechaTransaccion());

        if (request.bovinoId() != null) {
            Bovino bovino = bovinoRepository.findById(request.bovinoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bovino", "id", request.bovinoId()));
            transaccion.setBovino(bovino);
        } else {
            transaccion.setBovino(null);
        }

        if (request.loteSiembraId() != null) {
            LoteSiembra lote = loteSiembraRepository.findById(request.loteSiembraId())
                    .orElseThrow(() -> new ResourceNotFoundException("LoteSiembra", "id", request.loteSiembraId()));
            transaccion.setLoteSiembra(lote);
        } else {
            transaccion.setLoteSiembra(null);
        }
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if ((desde != null && hasta == null) || (desde == null && hasta != null)) {
            throw new BusinessRuleException("Para filtrar por fechas debe proporcionar tanto 'desde' como 'hasta'");
        }
        if (desde != null && desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }

    private BigDecimal normalizarEscala(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
