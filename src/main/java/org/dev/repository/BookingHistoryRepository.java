package org.dev.repository;

import org.dev.model.BookingHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingHistoryRepository extends CrudRepository<BookingHistory, Integer> {
}
