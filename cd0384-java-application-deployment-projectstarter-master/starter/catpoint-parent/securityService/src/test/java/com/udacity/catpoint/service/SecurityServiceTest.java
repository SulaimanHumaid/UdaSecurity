package com.udacity.catpoint.service;

import com.udacity.catpoint.FakeImageService;
import com.udacity.catpoint.application.SensorPanel;
import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    /**
     * Rigorous Test :-)
     */
    public SecurityService securityService;
    @Mock
    public FakeImageService imageService;
    @Mock
    public SecurityRepository securityRepository;

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    //1
    @Test
    public void if_alarmIsArmedAndSensorActivated_putSystemInPendingStatus() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.PENDING_ALARM);

    }

    //2
    @Test
    public void if_alarmIsArmedSensorActivatedAndInPendingStatus_putSystemInAlarmStatus() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);

    }

    //3
    @Test
    public void if_alarmPendingAndAllSensorsInactive_putSystemInNoAlarmStatus() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.NO_ALARM);
    }

    //4
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void if_alarmActive_ChangeSensorDoesNotAffectAlarmState(boolean isActive) {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(isActive);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }

    //5
    @Test
    public void if_sensorActivatedWhileAlreadyActivatedSystemInPendingState_ChangeToAlarmState() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }

    //6
    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    public void if_sensorDeactivatedWhileAlreadyInactive_MakeNoChangesToAlarmState(AlarmStatus alarmStatus) {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        securityService.changeSensorActivationStatus(sensor, false);
        Assertions.assertEquals(securityService.getAlarmStatus(), alarmStatus);
    }

    //7
    @Test
    public void if_thereIsCatAndArmedHome_ChangeToAlarmState() {
        BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }

    //8
    @Test
    public void if_thereIsNoCatAndSensorsNotActive_ChangeToNoAlarmStatus() {
        when(securityRepository.getSensors()).thenReturn(
                Set.of(new Sensor[]{new Sensor("0", SensorType.DOOR),
                        new Sensor("1", SensorType.WINDOW),
                        new Sensor("2", SensorType.MOTION)}));
        BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(false);
        securityService.processImage(bufferedImage);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.NO_ALARM);
    }

    //9
    @Test
    public void if_SystemDisarmed_SetNoAlarmStatus() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //10
    @Test
    public void if_SystemArmed_ResetAllSensorsToInactive() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

        when(securityRepository.getSensors()).thenReturn(
                Set.of(new Sensor[]{new Sensor("0", SensorType.DOOR),
                        new Sensor("1", SensorType.WINDOW),
                        new Sensor("2", SensorType.MOTION)}));
        Set<Sensor> sensors = securityService.getSensors();
        sensors.forEach((s) -> securityService.changeSensorActivationStatus(s, false));
        sensors.forEach((s) -> verify(securityRepository).updateSensor(s));
    }

    //11
    @Test
    public void if_thereIsCatAndArmedHome_ChangeToAlarmStateTest() {
        BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);

        securityService.processImage(bufferedImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }

    //12
    @Test
    public void if_noAlarmSensorActivated_putSystemInPendingStatus() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.PENDING_ALARM);
    }

    //13
    @Test
    public void if_thereIsCatAndArmedHome_ChangeToAlarmStateCover() {
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        verify(securityRepository).setArmingStatus(ArmingStatus.ARMED_HOME);
        Assertions.assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }

    //14
    @Test
    public void addSensorTest() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        securityService.addSensor(sensor);
        verify(securityRepository).addSensor(sensor);
    }

    //15
    @Test
    public void removeSensorTest() {
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        securityService.removeSensor(sensor);
        verify(securityRepository).removeSensor(sensor);
    }

    //15
    @Test
    public void addStatusListenerTest() {
        SensorPanel sensorPanel = new SensorPanel(securityService);
        securityService.addStatusListener((StatusListener) sensorPanel);
    }

    //16
    @Test
    public void removeStatusListenerTest() {
        SensorPanel sensorPanel = new SensorPanel(securityService);
        securityService.removeStatusListener((StatusListener) sensorPanel);
    }

}