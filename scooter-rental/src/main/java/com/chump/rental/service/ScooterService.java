package com.chump.rental.service;

import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dao.ScooterPendingRedisDao;
import com.chump.rental.dto.command.CreateScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.dto.response.ScooterResponse;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.ScooterModel;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScooterService {

    private final ScooterRepository scooterRepository;
    private final ScooterModelDao modelDao;
    private final ScooterMapper mapper;
    private final ScooterProducer scooterProducer;
    private final ScooterPendingRedisDao scooterPendingRedisDao;
    private final TransactionUtils transactionUtils;

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
                () -> new NoRequiredEntityException("No scooter found with id: " + scooterId)
        );

        ScooterStatus newStatus = switch (scooter.getStatus()) {
            case BLOCKING -> ScooterStatus.MAINTENANCE;
            case ACTIVATING -> ScooterStatus.OCCUPIED;
            case STOPPING -> ScooterStatus.FREE;
            default -> null; // Если статус не переходный, ничего не меняем
        };


        if (newStatus == null) return;
        scooter.setStatus(newStatus);

        transactionUtils.afterCommit(() -> scooterPendingRedisDao.deletePending(scooterId));
    }

    @Transactional
    public void handleStatusTimeout(int scooterId) {
        Scooter scooter = scooterRepository.findById(scooterId).orElseThrow(
                () -> new NoRequiredEntityException("No scooter found with id: " + scooterId)
        );

        switch (scooter.getStatus()) {
            case ACTIVATING -> {
                scooter.setStatus(ScooterStatus.FREE);
                scooterProducer.sendLock(scooterId);
                log.warn("Scooter with id: {} failed to get activated. " +
                        "Compensation 'lock' command sent and 'FREE' status forced", scooterId);
            }
            case BLOCKING -> {
                scooter.setStatus(ScooterStatus.MAINTENANCE);
                log.warn("Scooter with id: {} failed to force stop. " +
                        "'MAINTENANCE' status forced, 'lock' command is still pending", scooterId);
            }
            case STOPPING -> {
                // Свободный разблокированный самокат нежелательная ситуация
                // -> блокируется + статус `MAINTENANCE` наряду с forceStop.
                scooter.setStatus(ScooterStatus.MAINTENANCE);
                log.warn("Scooter with id: {} failed to stop. " +
                        "'MAINTENANCE' status forced, 'lock' command is still pending", scooterId);
            }
            default -> log.debug("Scooter with id: {} has no pending status. Skipped", scooterId);
        }
    }
}