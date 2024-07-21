package account.repositories;

import account.model.Account;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    Optional<Account> findAccountByEmailIgnoreCase(String email);
    @Modifying
    @Query(value = "UPDATE accounts SET password = :password WHERE email = :email", nativeQuery = true)
    void updateThePassword(@Param("email")String email, @Param("password")String password);


}
