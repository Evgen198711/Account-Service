package account.model;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"employee", "period"}))
public class SalaryDTO {
    private String employee;
    @DateTimeFormat(pattern = "MM-yyyy")
    private String period;
    @Min(0)
    private long salary;

    public SalaryDTO() {
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SalaryDTO salaryDTO = (SalaryDTO) o;

        if (!getEmployee().equals(salaryDTO.getEmployee())) return false;
        return getPeriod().equals(salaryDTO.getPeriod());
    }

    @Override
    public int hashCode() {
        int result = getEmployee().hashCode();
        result = 31 * result + getPeriod().hashCode();
        return result;
    }
}
