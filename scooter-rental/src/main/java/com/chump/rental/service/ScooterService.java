package com.chump.rental.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dto.command.CreateScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.ScooterModel;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScooterService {

    private final ScooterRepository scooterRepository;
    private final ScooterModelDao modelDao;
    private final ScooterMapper mapper;
    private final ScooterProducer scooterProducer;

    public ScooterService(ScooterRepository scooterRepository, ScooterModelDao modelDao, ScooterMapper mapper, ScooterProducer scooterProducer) {
        this.scooterRepository = scooterRepository;
        this.modelDao = modelDao;
        this.mapper = mapper;
        this.scooterProducer = scooterProducer;
    }

    @Transactional
    public ScooterResponse addScooter(CreateScooterCommand command) {
        // Тут Integer не нужен, т.к. модель обязательна
        int modelId = command.getModelId();
        ScooterModel model = modelDao.findById(modelId).orElseThrow(
                () -> new NoSuchEntityException("No scooter model found with id: " + modelId)
        );

        Scooter scooter = mapper.toEntity(command, model);
        return mapper.toResponse(scooterRepository.save(scooter));
    }

    @Transactional
    public ScooterResponse beginMaintenance(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.FREE) {
            throw new UnavaliableActionException("Unable to start maintenance for scooter with status: "
                    + scooter.getStatus());
        }

        scooter.setStatus(ScooterStatus.MAINTENANCE);
        return mapper.toResponse(scooter);
    }

    @Transactional
    public ScooterResponse finishMaintenance(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableActionException("Unable to finish maintenance for scooter without maintenance status");
        }

        scooter.setStatus(ScooterStatus.FREE);
        return mapper.toResponse(scooter);
    }

    @Transactional
    public ScooterResponse updateScooterInfo(int scooterId, UpdateScooterInfoCommand command) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableActionException("Start maintenance to update scooter's info");
        }

        Integer modelId = command.getModelId();
        ScooterModel model = modelId != null ? modelDao.findById(modelId).orElseThrow(
                () -> new NoSuchEntityException("No scooter model found with id: " + modelId)
        ) : null;

        mapper.updateScooterInfoFromCommand(command, model, scooter);
        return mapper.toResponse(scooter);
    }

    @Transactional
    public ScooterResponse rechargeScooterBattery(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableActionException("Start maintenance to replace scooter's battery");
        }

        scooter.setBattery(100);
        // TODO отправка уведомления в Kafka
        scooterProducer.sendRecharge(scooterId);

        return mapper.toResponse(scooter);
    }

    @Transactional
    public void writeOffScooter(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableActionException("Start maintenance to write off scooter");
        }

        scooterRepository.delete(scooterId);
    }

    @Transactional
    public void updateReceivedStatus(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        ScooterStatus newStatus = switch (scooter.getStatus()) {
            case BLOCKING -> ScooterStatus.MAINTENANCE;
            case ACTIVATING -> ScooterStatus.OCCUPIED;
            case STOPPING -> ScooterStatus.FREE;
            default -> scooter.getStatus(); // Если статус не переходный, оставляем
        };

        scooter.setStatus(newStatus);
    }
}