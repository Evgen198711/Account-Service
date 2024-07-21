package account.repositories;

import account.model.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    List<Group> findGroupByNameContains(String name);
}
