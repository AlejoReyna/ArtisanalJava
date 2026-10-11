package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;

import java.util.List;

/** Port of {@code Repositories/ITransparencyRecordRepository.cs}. */
public interface TransparencyRecordRepository {
    void addRange(List<TransparencyRecord> records);

    List<TransparencyRecord> getRecent(int count);
}
