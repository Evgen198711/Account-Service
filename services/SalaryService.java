package account.services;

import account.model.Account;
import account.model.Salary;
import account.model.SalaryReportDTO;
import account.repositories.SalaryRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalaryService {

    private SalaryRepository salaryRepository;

    public SalaryService(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }


    public void savePayrolls (List<Salary> payrolls) {
        salaryRepository.saveAll(payrolls);
    }

    public void updateSalary(Salary salary) {
        long value = salary.getSalary();
        long employee = salary.getAccount().getId();
        LocalDate period = salary.getPeriod();

        salaryRepository.updateSalary(value, employee, period);
    }

    public SalaryReportDTO getSalaryReport(AccountService accService, UserDetails details, String period) {

        LocalDate periodGenerated = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy")).atEndOfMonth();

        Account user = accService.findAccountByEmail(details.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not  found!"));

        Salary report = salaryRepository.getSalaryReport(user.getId(), periodGenerated).get(0);


        return new SalaryReportDTO(
                user.getName(),
                user.getLastname(),
                YearMonth.from(report.getPeriod()).format(DateTimeFormatter.ofPattern("MMMM-yyyy")),
                "%d dollar(s) %d cent(s)".formatted(report.getSalary()/100, report.getSalary()%100)
        );


    }

    public List<SalaryReportDTO> getSalaryReports(AccountService accService, UserDetails details) {
        Account user = accService.findAccountByEmail(details.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        List<Salary> reports = salaryRepository.getAllSalaryReports(user.getId());

        return reports.stream()
                .map(s -> new SalaryReportDTO(
                        user.getName(),
                        user.getLastname(),
                        YearMonth.from(s.getPeriod()).format(DateTimeFormatter.ofPattern("MMMM-yyyy")),
                        "%d dollar(s) %d cent(s)".formatted(s.getSalary()/100, s.getSalary()%100)
                )).toList();
    }
}
