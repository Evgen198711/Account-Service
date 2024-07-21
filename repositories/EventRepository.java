package account.repositories;

import account.events.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

    @Query(value = "SELECT * FROM event WHERE id =(SELECT MAX(id) FROM event)", nativeQuery = true)
    public Optional<Event> findTheLastRecordForTheUser();

    @Query(value = "SELECT counter FROM event WHERE id =(SELECT MAX(id) FROM event)", nativeQuery = true)
    public int getCounterValue();

}
