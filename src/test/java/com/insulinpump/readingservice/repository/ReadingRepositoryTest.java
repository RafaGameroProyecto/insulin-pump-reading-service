package com.insulinpump.readingservice.repository;

import com.insulinpump.readingservice.model.Reading;
import com.insulinpump.readingservice.model.ReadingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class ReadingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReadingRepository readingRepository;

    @Test
    @Transactional
    void should_find_readings_by_device_id() {
        // Given
        Reading reading1 = createTestReading(100.0f, 1L);
        Reading reading2 = createTestReading(150.0f, 1L);
        Reading reading3 = createTestReading(80.0f, 2L);

        entityManager.persist(reading1);
        entityManager.persist(reading2);
        entityManager.persist(reading3);
        entityManager.flush();

        // When
        List<Reading> readings = readingRepository.findByDeviceId(1L);

        // Then
        assertThat(readings).hasSize(2);
        assertThat(readings).extracting(Reading::getDeviceId).containsOnly(1L);
    }

    @Test
    @Transactional
    void should_find_readings_by_status() {
        // Given
        Reading normalReading = createTestReading(100.0f, 1L);
        normalReading.setStatus(ReadingStatus.NORMAL);

        Reading highReading = createTestReading(200.0f, 1L);
        highReading.setStatus(ReadingStatus.HIGH);

        entityManager.persist(normalReading);
        entityManager.persist(highReading);
        entityManager.flush();

        // When
        List<Reading> highReadings = readingRepository.findByStatus(ReadingStatus.HIGH);

        // Then
        assertThat(highReadings).hasSize(1);
        assertThat(highReadings.get(0).getStatus()).isEqualTo(ReadingStatus.HIGH);
    }

    private Reading createTestReading(Float glucoseLevel, Long deviceId) {
        Reading reading = new Reading();
        reading.setGlucoseLevel(glucoseLevel);
        reading.setDeviceId(deviceId);
        reading.setStatus(ReadingStatus.NORMAL);
        reading.setTimestamp(LocalDateTime.now());
        reading.setManualReading(false);
        reading.setRequiresAction(false);
        return reading;
    }
}
