package com.chump.rental.service;

import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableAction;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dto.command.ScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.ScooterModel;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScooterService {

    private final ScooterRepository repo;
    private final ScooterModelDao modelDao;
    private final ScooterMapper mapper;

    public ScooterService(ScooterRepository repo, ScooterModelDao modelDao, ScooterMapper mapper) {
        this.repo = repo;
        this.modelDao = modelDao;
        this.mapper = mapper;
    }

    @Transactional
    public ScooterResponse addScooter(ScooterCommand command) {
        // Тут Integer не нужен, т.к. модель обязательна
        int modelId = command.getModelId();
        ScooterModel model = modelDao.findById(modelId).orElseThrow(
                () -> new NoSuchEntityException("No scooter model found with id: " + modelId)
        );

        Scooter scooter = mapper.toEntity(command, model);
        return mapper.toResponse(repo.save(scooter));
    }

    @Transactional
    public ScooterResponse startMaintenance(int scooterId) {
        Scooter scooter = repo.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );
        scooter.setStatus(ScooterStatus.MAINTENANCE);

        repo.update(scooter); // TODO Явный update, для синхронизации Redis и БД
        return mapper.toResponse(scooter);
    }

    @Transactional
    public ScooterResponse updateScooterInfo(int scooterId, UpdateScooterInfoCommand command) {
        Scooter scooter = repo.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableAction("Start maintenance to update scooter's info");
        }

        Integer modelId = command.getModelId();
        ScooterModel model = modelId != null ? modelDao.findById(modelId).orElseThrow(
                () -> new NoSuchEntityException("No scooter model found with id: " + modelId)
        ) : null;

        mapper.updateScooterInfoFromCommand(command, model, scooter);

//        repo.update(scooter); // TODO Явный update, для синхронизации Redis и БД (не нужно т.к. обновляем только Postgres)
        return mapper.toResponse(scooter);
    }

    @Transactional
    public ScooterResponse updateScooterBattery(int scooterId, int battery) {
        Scooter scooter = repo.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        scooter.setBattery(battery);
        repo.updateBattery(scooterId, battery); // Redis sync
        // TODO отправка уведомления в Kafka

        return mapper.toResponse(scooter);
    }

    @Transactional
    public void writeOffScooter(int scooterId) {
        Scooter scooter = repo.findById(scooterId).orElseThrow(
                () -> new NoSuchEntityException("No scooter found with id: " + scooterId)
        );

        if (scooter.getStatus() != ScooterStatus.MAINTENANCE) {
            throw new UnavaliableAction("Start maintenance to write off scooter");
        }

        repo.delete(scooterId);
    }
}