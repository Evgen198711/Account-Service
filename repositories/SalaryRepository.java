package account.repositories;

import account.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    @Modifying
    @Query(value = "UPDATE salaries SET salary =:salary WHERE employee =:employee AND period =:period", nativeQuery = true)
    public void updateSalary(@Param("salary")long salary, @Param("employee")long employee, @Param("period")LocalDate period);

    @Query(value = "SELECT * FROM salaries WHERE employee =:employee AND period =:period", nativeQuery = true)
    List<Salary> getSalaryReport(@Param("employee")long employee, @Param("period")LocalDate period);
    @Query(value = "SELECT * FROM salaries WHERE employee = :employee ORDER BY period DESC", nativeQuery = true)
    List<Salary> getAllSalaryReports(@Param("employee")long employee);
}
