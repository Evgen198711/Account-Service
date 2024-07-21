package account.controllers;

import account.events.EventName;
import account.exceptions.NotUniqueValuesSentException;
import account.exceptions.UserExistException;
import account.model.*;
import account.services.AccountService;
import account.services.EventService;
import account.services.GroupService;
import account.services.SalaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final SalaryService salaryService;
    private final GroupService groupService;
    private final EventService eventService;

    public AccountController(AccountService accountService,
                             PasswordEncoder passwordEncoder,
                             SalaryService salaryService,
                             GroupService groupService,
                             EventService eventService) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.salaryService = salaryService;
        this.groupService = groupService;
        this.eventService = eventService;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signupAccount (@RequestBody @Valid Account account, @AuthenticationPrincipal UserDetails details) {


        accountService.passwordChecker(account.getPassword());

        Account acc = new Account();
        acc.setName(account.getName());
        acc.setLastname(account.getLastname());
        acc.setEmail(account.getEmail().toLowerCase());
        acc.setPassword(passwordEncoder.encode(account.getPassword()));

        if(!accountService.findAllAccounts().iterator().hasNext()) {
            acc.getUserGroups().add(groupService.findGroupByName("ADMINISTRATOR"));
        } else {
            acc.getUserGroups().add(groupService.findGroupByName("USER"));
        }


            if (accountService.findAccountByEmail(acc.getEmail())
                    .isPresent()) {
                throw new UserExistException();
            }

            Account returnedAccount = accountService.saveAccount(acc);
            AccountWithRolesDTO accWithRoles = accountService.accountWithRolesDTOMapper(returnedAccount);

            eventService.publishEvent(EventName.CREATE_USER,
                    details == null ? "Anonymous" : details.getUsername(),
                    returnedAccount.getEmail());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(accWithRoles);

    }
@Secured({"ROLE_USER", "ROLE_ACCOUNTANT"})
    @GetMapping("/empl/payment")
    public ResponseEntity<?> makePayment(@RequestParam(required = false) String period, @AuthenticationPrincipal UserDetails details) {

        if(period == null) {
            List<SalaryReportDTO> reports = salaryService.getSalaryReports(accountService, details);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reports);

        } else {

            SalaryReportDTO report = salaryService.getSalaryReport(accountService, details, period);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(report);
        }
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<?> changePass(@RequestBody PasswordDTO newPass, @AuthenticationPrincipal UserDetails details) {

        ChangeDeleteResponse response = accountService.changePassword(passwordEncoder, details, newPass);

        eventService.publishEvent(EventName.CHANGE_PASSWORD, details.getUsername(), details.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @Transactional
    @PostMapping("/acct/payments")
    public ResponseEntity<?> uploadPayrolls(@RequestBody @Valid List<SalaryDTO> payrolls) {

        if(payrolls.size() != new HashSet<>(payrolls).size()) {
            throw new NotUniqueValuesSentException();
        }

        List<Salary> generatedPayrolls = payrolls.stream()
                .map(accountService::salaryMapper)
                        .toList();
        salaryService.savePayrolls(generatedPayrolls);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("status", "Added successfully!"));
    }

    @Transactional
    @PutMapping("/acct/payments")
    public ResponseEntity<?> updateSalary(@RequestBody @Valid SalaryDTO salary) {
        Salary generatedSalary = accountService.salaryMapper(salary);
        salaryService.updateSalary(generatedSalary);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("status", "Updated successfully!"));
    }

    @GetMapping("/admin/user/")
    public ResponseEntity<?> findUsers() {
        List<AccountWithRolesDTO> list = accountService.accountWithRolesDTOList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(list);
    }

    @DeleteMapping("/admin/user/{user_email}")
    public ResponseEntity<?> deleteUser(@PathVariable("user_email") String user_email, @AuthenticationPrincipal UserDetails details) {
        DeleteResponseDTO response = accountService.deleteAccount(user_email);

        eventService.publishEvent(EventName.DELETE_USER, details.getUsername(), user_email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
    @Transactional
    @PutMapping("/admin/user/role")
    public ResponseEntity<?> changeUserRole(@RequestBody ChangeRoleDTO role, @AuthenticationPrincipal UserDetails details) {
        String object = "";
        EventName eventName= EventName.GRANT_ROLE;

        Account account = accountService.findAccountByEmail(role.getUser())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if(role.getOperation().equalsIgnoreCase("GRANT")) {
            account = accountService.addRole(groupService, role, account);
            object = "Grant role %s to %s".formatted(role.getRole(), account.getEmail());
        }

        if(role.getOperation().equalsIgnoreCase("REMOVE")) {
            account = accountService.deleteRole(groupService, role, account);
            object = "Remove role %s from %s".formatted(role.getRole(), account.getEmail());
            eventName = EventName.REMOVE_ROLE;
        }

        eventService.publishEvent(eventName, details.getUsername(), object);

        return   ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.accountWithRolesDTOMapper(account));
    }

    @GetMapping("/security/events/")
    public ResponseEntity<?> getEvents() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getAllEvents());
    }

    @PutMapping("/admin/user/access")
    public ResponseEntity<?> lockUnlockAccount(@RequestBody LockAccountDTO accountDTO, @AuthenticationPrincipal UserDetails details) {
        Account acc = accountService.findAccountByEmail(accountDTO.getUser()).orElseThrow(() ->
                new UsernameNotFoundException("User not fount!"));



        Account finalAccount = accountService.lockUnlockAccount(acc, accountDTO.getOperation());

        String status = finalAccount.isLocked() ? "locked" : "unlocked";

        if("unlocked".equalsIgnoreCase(status)) {
            eventService.publishEvent(EventName.UNLOCK_USER,
                    details.getUsername(),
                    "Unlock user %s".formatted(finalAccount.getEmail()));
        }

        if("locked".equalsIgnoreCase(status)) {
            eventService.publishEvent(EventName.LOCK_USER,
                    details.getUsername(),
                    "Lock user %s".formatted(finalAccount.getEmail()));
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("status", "User %s %s!".formatted(acc.getEmail(), status)));
    }
}
