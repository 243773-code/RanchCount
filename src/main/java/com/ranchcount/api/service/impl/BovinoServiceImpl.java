package com.ranchcount.api.service.impl;

import com.ranchcount.api.dto.request.BovinoCreateRequest;
import com.ranchcount.api.dto.request.BovinoHembraCreateRequest;
import com.ranchcount.api.dto.request.BovinoMachoCreateRequest;
import com.ranchcount.api.dto.request.BovinoUpdateRequest;
import com.ranchcount.api.dto.request.MontaRequest;
import com.ranchcount.api.dto.request.PartoRequest;
import com.ranchcount.api.dto.response.BovinoResponse;
import com.ranchcount.api.entity.Bovino;
import com.ranchcount.api.entity.EstadoBovino;
import com.ranchcount.api.entity.SexoBovino;
import com.ranchcount.api.exception.BusinessRuleException;
import com.ranchcount.api.exception.DuplicateResourceException;
import com.ranchcount.api.exception.ResourceNotFoundException;
import com.ranchcount.api.repository.BovinoRepository;
import com.ranchcount.api.service.BovinoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BovinoServiceImpl implements BovinoService {

    private final BovinoRepository bovinoRepository;

    public BovinoServiceImpl(BovinoRepository bovinoRepository) {
        this.bovinoRepository = bovinoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BovinoResponse> listar(EstadoBovino estado, SexoBovino sexo) {
        List<Bovino> resultados;
        if (estado != null && sexo != null) {
            resultados = bovinoRepository.findByEstadoAndSexoOrderByIdDesc(estado, sexo);
        } else if (estado != null) {
            resultados = bovinoRepository.findByEstadoOrderByIdDesc(estado);
        } else if (sexo != null) {
            resultados = bovinoRepository.findBySexoOrderByIdDesc(sexo);
        } else {
            resultados = bovinoRepository.findAllByOrderByIdDesc();
        }
        return resultados.stream().map(BovinoResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BovinoResponse> listarHembras(EstadoBovino estado) {
        if (estado != null) {
            validarCompatibilidadSexoEstado(SexoBovino.HEMBRA, estado);
        }
        return listar(estado, SexoBovino.HEMBRA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BovinoResponse> listarMachos(EstadoBovino estado) {
        if (estado != null) {
            validarCompatibilidadSexoEstado(SexoBovino.MACHO, estado);
        }
        return listar(estado, SexoBovino.MACHO);
    }

    @Override
    @Transactional(readOnly = true)
    public BovinoResponse obtenerPorId(Long id) {
        Bovino bovino = obtenerEntidadPorId(id);
        return BovinoResponse.fromEntity(bovino);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BovinoResponse> listarProximosPartos(int diasVentana) {
        int dias = diasVentana > 0 ? diasVentana : 7;
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);

        return bovinoRepository
                .findByEstadoAndFechaEstimadaPartoBetweenOrderByFechaEstimadaPartoAsc(
                        EstadoBovino.CARGADA,
                        hoy.minusDays(7),
                        fechaLimite)
                .stream()
                .map(BovinoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public BovinoResponse crear(BovinoCreateRequest request) {
        return crearInterno(
                request.areteCodigo(),
                request.nombre(),
                request.raza(),
                request.sexo(),
                request.fechaNacimiento(),
                request.pesoKg(),
                request.estado(),
                request.fechaMonta(),
                request.notas());
    }

    @Override
    @Transactional
    public BovinoResponse crearHembra(BovinoHembraCreateRequest request) {
        return crearInterno(
                request.areteCodigo(),
                request.nombre(),
                request.raza(),
                SexoBovino.HEMBRA,
                request.fechaNacimiento(),
                request.pesoKg(),
                request.estado(),
                request.fechaMonta(),
                request.notas());
    }

    @Override
    @Transactional
    public BovinoResponse crearMacho(BovinoMachoCreateRequest request) {
        return crearInterno(
                request.areteCodigo(),
                request.nombre(),
                request.raza(),
                SexoBovino.MACHO,
                request.fechaNacimiento(),
                request.pesoKg(),
                request.estado(),
                null,
                request.notas());
    }

    @Override
    @Transactional
    public BovinoResponse actualizar(Long id, BovinoUpdateRequest request) {
        Bovino bovino = obtenerEntidadPorId(id);
        String codigoNormalizado = request.areteCodigo().trim().toUpperCase();

        if (!bovino.getAreteCodigo().equalsIgnoreCase(codigoNormalizado)
                && bovinoRepository.existsByAreteCodigoIgnoreCase(codigoNormalizado)) {
            throw new DuplicateResourceException("Ya existe otro bovino con el arete: " + codigoNormalizado);
        }

        validarCompatibilidadSexoEstado(request.sexo(), request.estado());

        bovino.setAreteCodigo(codigoNormalizado);
        bovino.setNombre(limpiarTexto(request.nombre()));
        bovino.setRaza(request.raza().trim());
        bovino.setSexo(request.sexo());
        bovino.setFechaNacimiento(request.fechaNacimiento());
        bovino.setPesoKg(request.pesoKg());
        bovino.setEstado(request.estado());
        bovino.setNotas(limpiarTexto(request.notas()));

        aplicarReglaGestacion(bovino, request.estado(), request.fechaMonta());

        return BovinoResponse.fromEntity(bovinoRepository.save(bovino));
    }

    @Override
    @Transactional
    public BovinoResponse registrarMonta(Long id, MontaRequest request) {
        Bovino bovino = obtenerEntidadPorId(id);

        if (bovino.getSexo() != SexoBovino.HEMBRA) {
            throw new BusinessRuleException("Solo se puede registrar monta y estado CARGADA en ejemplares HEMBRA");
        }
        if (bovino.getEstado() == EstadoBovino.VENDIDO || bovino.getEstado() == EstadoBovino.BAJA) {
            throw new BusinessRuleException(
                    "No se puede registrar monta en un bovino con estado " + bovino.getEstado());
        }

        boolean tieneFecha = request.fechaMonta() != null;
        boolean tieneDiasAprox = request.diasGestacionAproximados() != null;
        if (tieneFecha == tieneDiasAprox) {
            throw new BusinessRuleException(
                    "Debe proporcionar exactamente uno: fechaMonta (inseminacion/monta observada) o diasGestacionAproximados (monta natural)");
        }

        LocalDate fechaMontaEfectiva = tieneFecha
                ? request.fechaMonta()
                : LocalDate.now().minusDays(request.diasGestacionAproximados());

        if (bovino.getFechaNacimiento() != null && fechaMontaEfectiva.isBefore(bovino.getFechaNacimiento())) {
            throw new BusinessRuleException(
                    "La fecha de monta no puede ser anterior a la fecha de nacimiento del bovino");
        }

        bovino.setEstado(EstadoBovino.CARGADA);
        bovino.setFechaMonta(fechaMontaEfectiva);
        bovino.setFechaEstimadaParto(fechaMontaEfectiva.plusDays(DIAS_GESTACION_BOVINA));
        bovino.setUltimaAlertaParto(null);

        if (request.notas() != null && !request.notas().isBlank()) {
            bovino.setNotas(request.notas().trim());
        }

        return BovinoResponse.fromEntity(bovinoRepository.save(bovino));
    }

    @Override
    @Transactional
    public BovinoResponse registrarParto(Long id, PartoRequest request) {
        Bovino madre = obtenerEntidadPorId(id);

        if (madre.getSexo() != SexoBovino.HEMBRA) {
            throw new BusinessRuleException("El ejemplar indicado no es HEMBRA");
        }
        if (madre.getEstado() != EstadoBovino.CARGADA) {
            throw new BusinessRuleException(
                    "Solo se puede reportar parto de una vaca cuyo estado actual sea CARGADA (estado actual: "
                            + madre.getEstado() + ")");
        }
        if (madre.getFechaMonta() != null && request.fechaParto().isBefore(madre.getFechaMonta())) {
            throw new BusinessRuleException("La fecha de parto no puede ser anterior a la fecha de monta registrada");
        }

        // Si el operador envia datos de areteCria, validamos y creamos el becerro/a en
        // la misma transaccion
        if (request.areteCria() != null && !request.areteCria().isBlank()) {
            if (!Boolean.TRUE.equals(request.partoExitoso())) {
                throw new BusinessRuleException("No se puede registrar arete de cria cuando partoExitoso es false");
            }
            if (request.sexoCria() == null) {
                throw new BusinessRuleException("El campo sexoCria es obligatorio cuando se proporciona areteCria");
            }
            String areteCriaNorm = request.areteCria().trim().toUpperCase();
            if (bovinoRepository.existsByAreteCodigoIgnoreCase(areteCriaNorm)) {
                throw new DuplicateResourceException(
                        "Ya existe un bovino registrado con el arete de cria: " + areteCriaNorm);
            }

            Bovino cria = new Bovino();
            cria.setAreteCodigo(areteCriaNorm);
            cria.setNombre(limpiarTexto(request.nombreCria()));
            cria.setRaza(madre.getRaza());
            cria.setSexo(request.sexoCria());
            cria.setFechaNacimiento(request.fechaParto());
            cria.setPesoKg(request.pesoCriaKg());
            cria.setEstado(request.sexoCria() == SexoBovino.HEMBRA ? EstadoBovino.VACIA : EstadoBovino.ENGORDA);
            cria.setNotas("Cria nacida de madre con arete " + madre.getAreteCodigo());
            bovinoRepository.save(cria);
        }

        madre.setEstado(Boolean.TRUE.equals(request.partoExitoso()) ? EstadoBovino.LACTANDO : EstadoBovino.VACIA);
        madre.setFechaUltimoParto(request.fechaParto());
        madre.setFechaMonta(null);
        madre.setFechaEstimadaParto(null);
        madre.setUltimaAlertaParto(null);

        if (request.notas() != null && !request.notas().isBlank()) {
            madre.setNotas(request.notas().trim());
        }

        return BovinoResponse.fromEntity(bovinoRepository.save(madre));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Bovino bovino = obtenerEntidadPorId(id);
        bovinoRepository.delete(bovino);
    }

    private BovinoResponse crearInterno(
            String areteCodigo,
            String nombre,
            String raza,
            SexoBovino sexo,
            LocalDate fechaNacimiento,
            BigDecimal pesoKg,
            EstadoBovino estado,
            LocalDate fechaMonta,
            String notas) {
        String codigoNormalizado = areteCodigo.trim().toUpperCase();
        if (bovinoRepository.existsByAreteCodigoIgnoreCase(codigoNormalizado)) {
            throw new DuplicateResourceException("Ya existe un bovino registrado con el arete: " + codigoNormalizado);
        }

        validarCompatibilidadSexoEstado(sexo, estado);

        Bovino bovino = new Bovino();
        bovino.setAreteCodigo(codigoNormalizado);
        bovino.setNombre(limpiarTexto(nombre));
        bovino.setRaza(raza.trim());
        bovino.setSexo(sexo);
        bovino.setFechaNacimiento(fechaNacimiento);
        bovino.setPesoKg(pesoKg);
        bovino.setEstado(estado);
        bovino.setNotas(limpiarTexto(notas));

        aplicarReglaGestacion(bovino, estado, fechaMonta);

        return BovinoResponse.fromEntity(bovinoRepository.save(bovino));
    }

    private Bovino obtenerEntidadPorId(Long id) {
        return bovinoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bovino", "id", id));
    }

    private void validarCompatibilidadSexoEstado(SexoBovino sexo, EstadoBovino estado) {
        if (sexo == SexoBovino.MACHO
                && (estado == EstadoBovino.CARGADA || estado == EstadoBovino.VACIA
                        || estado == EstadoBovino.LACTANDO)) {
            throw new BusinessRuleException(
                    "Un ejemplar MACHO no puede asignarse al estado reproductivo femenino " + estado);
        }
        if (sexo == SexoBovino.HEMBRA && estado == EstadoBovino.SEMENTAL) {
            throw new BusinessRuleException("Un ejemplar HEMBRA no puede asignarse al estado SEMENTAL");
        }
    }

    private void aplicarReglaGestacion(Bovino bovino, EstadoBovino nuevoEstado, LocalDate fechaMontaSolicitada) {
        if (nuevoEstado == EstadoBovino.CARGADA) {
            LocalDate fechaMontaEfectiva = fechaMontaSolicitada != null
                    ? fechaMontaSolicitada
                    : (bovino.getFechaMonta() != null ? bovino.getFechaMonta() : LocalDate.now());

            if (bovino.getFechaNacimiento() != null && fechaMontaEfectiva.isBefore(bovino.getFechaNacimiento())) {
                throw new BusinessRuleException("La fecha de monta no puede ser anterior a la fecha de nacimiento");
            }

            bovino.setFechaMonta(fechaMontaEfectiva);
            bovino.setFechaEstimadaParto(fechaMontaEfectiva.plusDays(DIAS_GESTACION_BOVINA));
            bovino.setUltimaAlertaParto(null);
        } else {
            bovino.setFechaMonta(null);
            bovino.setFechaEstimadaParto(null);
            bovino.setUltimaAlertaParto(null);
        }
    }

    private String limpiarTexto(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
