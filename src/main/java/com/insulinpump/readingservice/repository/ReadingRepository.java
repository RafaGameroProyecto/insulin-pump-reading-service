package com.insulinpump.readingservice.repository;

import com.insulinpump.readingservice.model.Reading;
import com.insulinpump.readingservice.model.ReadingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    List<Reading> findByDeviceId(Long deviceId);
    List<Reading> findByDeviceIdAndTimestampBetween(Long deviceId, LocalDateTime start, LocalDateTime end);
    List<Reading> findByDeviceIdAndStatus(Long deviceId, ReadingStatus status);
    List<Reading> findByDeviceIdOrderByTimestampDesc(Long deviceId);
    List<Reading> findByStatus(ReadingStatus status);
    List<Reading> findByRequiresActionTrue();

    @Query("SELECT r FROM Reading r WHERE r.deviceId = :deviceId ORDER BY r.timestamp DESC LIMIT 1")
    Optional<Reading> findLatestByDeviceId(@Param("deviceId") Long deviceId);

    @Query("SELECT AVG(r.glucoseLevel) FROM Reading r WHERE r.deviceId = :deviceId AND r.timestamp BETWEEN :start AND :end")
    Float findAverageGlucoseLevelByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reading r WHERE r.deviceId = :deviceId AND r.status IN ('LOW', 'CRITICAL_LOW') AND r.timestamp BETWEEN :start AND :end")
    Long countLowReadingsByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reading r WHERE r.deviceId = :deviceId AND r.status IN ('HIGH', 'CRITICAL_HIGH') AND r.timestamp BETWEEN :start AND :end")
    Long countHighReadingsByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT MIN(r.glucoseLevel) FROM Reading r WHERE r.deviceId = :deviceId AND r.timestamp BETWEEN :start AND :end")
    Float findMinGlucoseLevelByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT MAX(r.glucoseLevel) FROM Reading r WHERE r.deviceId = :deviceId AND r.timestamp BETWEEN :start AND :end")
    Float findMaxGlucoseLevelByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}