package com.chump.rental.service;

import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavaliableActionException;
import com.chump.common.utils.TransactionUtils;
import com.chump.rental.dao.ScooterModelDao;
import com.chump.rental.dao.ScooterPendingRedisDao;
import com.chump.rental.dto.command.CreateScooterCommand;
import com.chump.rental.dto.command.UpdateScooterInfoCommand;
import com.chump.rental.kafka.ScooterProducer;
import com.chump.rental.mapper.ScooterMapper;
import com.chump.rental.model.Scooter;
import com.chump.rental.model.status.ScooterStatus;
import com.chump.rental.repo.ScooterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Scooter service testing")
@ExtendWith(MockitoExtension.class)
public class ScooterServiceTest {

    @Mock private ScooterRepository scooterRepository;
    @Mock private ScooterModelDao modelDao;
    @Mock private ScooterMapper mapper;
    @Mock private ScooterProducer scooterProducer;
    @Mock private ScooterPendingRedisDao scooterPendingRedisDao;
    @Mock private TransactionUtils transactionUtils;

    @InjectMocks
    private ScooterService service;

    @Test
    @Tag("unit")
    @DisplayName("Add scooter method should throw exception, if model ID is unknown")
    public void addScooterShouldThrowWhenUnknownModelId() {
        when(modelDao.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.addScooter(CreateScooterCommand.builder().modelId(1).build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown model ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Begin maintenance method should throw exception, if scooter ID is unknown")
    public void beginMaintenanceShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.beginMaintenance(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Begin maintenance method should throw exception, if scooter is not free")
    public void beginMaintenanceShouldThrowWhenNotFreeScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.beginMaintenance(1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Begin maintenance method should update scooter status to MAINTENANCE")
    public void beginMaintenanceShouldUpdateStatus() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.FREE
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));

        service.beginMaintenance(1);
        assertEquals(ScooterStatus.MAINTENANCE, scooter.getStatus());
    }

    @Test
    @Tag("unit")
    @DisplayName("Finish maintenance method should throw exception, if scooter ID is unknown")
    public void finishMaintenanceShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.finishMaintenance(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Finish maintenance method should throw exception, if scooter's status is not MAINTENANCE")
    public void finishMaintenanceShouldThrowWhenNotMaintenanceScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class, () -> service.finishMaintenance(1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Finish maintenance method should update scooter status to FREE")
    public void finishMaintenanceShouldUpdateStatus() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.MAINTENANCE
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));

        service.finishMaintenance(1);
        assertEquals(ScooterStatus.FREE, scooter.getStatus());
    }

    @Test
    @Tag("unit")
    @DisplayName("Update scooter info method should throw exception, if scooter ID is unknown")
    public void updateScooterInfoShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateScooterInfo(1, UpdateScooterInfoCommand.builder().build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update info method should throw exception, if scooter's status is not maintenance")
    public void updateScooterInfoShouldThrowWhenNotMaintenanceScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.updateScooterInfo(1, UpdateScooterInfoCommand.builder().build()));
    }

    @Test
    @Tag("unit")
    @DisplayName("Update info method should throw exception, if scooter model ID is unknown")
    public void updateScooterInfoShouldThrowWhenUnknownScooterModelId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.MAINTENANCE
                )
        ));

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.updateScooterInfo(1, UpdateScooterInfoCommand.builder().modelId(1).build()));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter model ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Recharge scooter method should throw exception, if scooter ID is unknown")
    public void rechargeScooterShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.rechargeScooterBattery(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Recharge scooter battery method should throw exception, if scooter's status is not maintenance")
    public void rechargeScooterShouldThrowWhenNotMaintenanceScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.rechargeScooterBattery(1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Recharge scooter method should update battery and send recharge")
    public void rechargeScooterShouldUpdateScooterAndSendRecharge() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.MAINTENANCE
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.rechargeScooterBattery(1);

        assertEquals(100, scooter.getBattery());
        verify(scooterProducer, only()).sendRecharge(anyInt());
    }

    @Test
    @Tag("unit")
    @DisplayName("Write off scooter method should throw exception, if scooter ID is unknown")
    public void writeOffShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoSuchEntityException exception = assertThrows(NoSuchEntityException.class,
                () -> service.writeOffScooter(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Write off scooter method should throw exception, if scooter's status is not maintenance")
    public void writeOffScooterShouldThrowWhenNotMaintenanceScooter() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(
                new Scooter(
                        1,
                        null,
                        null,
                        null,
                        null,
                        ScooterStatus.OCCUPIED
                )
        ));

        assertThrows(UnavaliableActionException.class,
                () -> service.writeOffScooter(1));
    }

    @Test
    @Tag("unit")
    @DisplayName("Update scooter status method should throw exception, if scooter ID is unknown")
    public void updateStatusShouldThrowWhenUnknownScooterId() {
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.empty());

        NoRequiredEntityException exception = assertThrows(NoRequiredEntityException.class,
                () -> service.updateReceivedStatus(1));
        assertTrue(exception.getMessage().contains("1"),
                "Exception message should contain unknown scooter ID");
    }

    @Test
    @Tag("unit")
    @DisplayName("Update scooter status method must update BLOCKING to MAINTENANCE and delete pending")
    public void updateStatusShouldBlockingToMaintenanceAndDelPending() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.BLOCKING
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.updateReceivedStatus(1);

        assertEquals(ScooterStatus.MAINTENANCE, scooter.getStatus());
        verify(scooterPendingRedisDao, only()).deletePending(anyInt());
    }

    @Test
    @Tag("unit")
    @DisplayName("Update scooter status method must update ACTIVATING to OCCUPIED and delete pending")
    public void updateStatusShouldActivatingToOccupiedAndDelPending() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.ACTIVATING
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.updateReceivedStatus(1);

        assertEquals(ScooterStatus.OCCUPIED, scooter.getStatus());
        verify(scooterPendingRedisDao, only()).deletePending(anyInt());
    }

    @Test
    @Tag("unit")
    @DisplayName("Update scooter status method must update STOPPING to FREE and delete pending")
    public void updateStatusShouldStoppingToFreeAndDelPending() {
        Scooter scooter = new Scooter(
                1,
                null,
                null,
                null,
                null,
                ScooterStatus.STOPPING
        );
        when(scooterRepository.findById(anyInt())).thenReturn(Optional.of(scooter));
        doAnswer(invocationOnMock -> {
            Runnable action = invocationOnMock.getArgument(0);
            action.run();
            return null;
        }).when(transactionUtils).afterCommit(any());

        service.updateReceivedStatus(1);

        assertEquals(ScooterStatus.FREE, scooter.getStatus());
        verify(scooterPendingRedisDao, only()).deletePending(anyInt());
    }
}
