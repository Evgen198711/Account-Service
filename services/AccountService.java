package account.services;

import account.events.EventName;
import account.exceptions.*;
import account.model.*;
import account.repositories.AccountRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final List<String> breachedPasswords = List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;

    }

    public Iterable<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> findAccountByEmail(String email) {
        return accountRepository.findAccountByEmailIgnoreCase(email);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, LockedException {
        Account acc = accountRepository
                .findAccountByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found!"));


        UserDetails user = User.builder().username(
                        acc.getEmail())
                .password(acc.getPassword())
                .authorities(acc.getUserGroups().stream().map(Group::getName).map(SimpleGrantedAuthority::new).toList())
                .accountLocked(acc.isLocked())
                .build();


        return user;
    }

    public void passwordChecker(String password) {
        if (password.length() < 12) {
            throw new PasswordLengthException();
        }

        if (breachedPasswords.contains(password)) {
            throw new PasswordInHackerDatabaseException();
        }
    }

    @Transactional
    public ChangeDeleteResponse changePassword(PasswordEncoder encoder, UserDetails details, PasswordDTO newPassword) throws SamePasswordsException {

        ChangeDeleteResponse response = new ChangeDeleteResponse();

        passwordChecker(newPassword.getNewPassword());

        response.setEmail(details.getUsername());

        if (encoder.matches(newPassword.getNewPassword(), accountRepository.findAccountByEmailIgnoreCase(details.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found!"))
                .getPassword())) {
            throw new SamePasswordsException();

        } else {
            accountRepository.updateThePassword(details.getUsername(),
                    encoder.encode(newPassword.getNewPassword()));

            response.setStatus("The password has been updated successfully");
            return response;
        }
    }

    public Salary salaryMapper(SalaryDTO receivedPayroll) {
        Salary salary = new Salary();

        salary.setAccount(accountRepository.findAccountByEmailIgnoreCase(receivedPayroll.getEmployee())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!")));
        salary.setPeriod(YearMonth.parse(receivedPayroll.getPeriod(), DateTimeFormatter.ofPattern("MM-yyyy")).atEndOfMonth());
        salary.setSalary(receivedPayroll.getSalary());

        return salary;
    }

    public List<AccountWithRolesDTO> accountWithRolesDTOList() {

        List<Account> accs = new ArrayList<>();
        findAllAccounts().forEach(accs::add);

        return accs.stream()
                .map(this::accountWithRolesDTOMapper).sorted(Comparator.comparing(AccountWithRolesDTO::getId)).toList();

    }

    @Transactional
    public DeleteResponseDTO deleteAccount(String email) {
        Account account = findAccountByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if (account.getUserGroups().stream().map(Group::getName).anyMatch(r -> r.equals("ROLE_ADMINISTRATOR"))) {
            throw new AdminRemoveException();
        }
        accountRepository.delete(account);

        DeleteResponseDTO response = new DeleteResponseDTO();
        response.setUser(account.getEmail());
        response.setStatus("Deleted successfully!");
        return response;
    }

    public AccountWithRolesDTO accountWithRolesDTOMapper(Account account) {
        return new AccountWithRolesDTO.Builder()
                .setId(account.getId())
                .setName(account.getName())
                .setLastname(account.getLastname())
                .setEmail(account.getEmail())
                .setRoles(account.getUserGroups().stream().map(Group::getName).sorted().toList())
                .build();
    }

    public Account addRole(GroupService groupService, ChangeRoleDTO roleToAdd, Account acc) {
        if (acc.getUserGroups().stream().map(Group::getGroup).anyMatch(s -> roleToAdd.getRights().equals(s))) {
            if (acc.getUserGroups().stream().map(Group::getName).noneMatch(s -> roleToAdd.getRole().equals(s))) {
                Group group = groupService.findGroupByName(roleToAdd.getRole());
                acc.getUserGroups().add(group);


            }
        } else {
            Group group = groupService.findGroupByName(roleToAdd.getRole());
            throw new CustomException("The user cannot combine administrative and business roles!");
        }
        return saveAccount(acc);
    }

    public Account deleteRole(GroupService groupService, ChangeRoleDTO roleToAdd, Account acc) {
        if (acc.getUserGroups().stream().map(Group::getName).noneMatch(s -> "ROLE_".concat(roleToAdd.getRole()).equals(s))) {
            throw new CustomException("The user does not have a role!");
        }

        if (acc.getUserGroups().stream().map(Group::getName).anyMatch("ROLE_ADMINISTRATOR"::equals)) {
            throw new CustomException("Can't remove ADMINISTRATOR role!");
        }

        if (acc.getUserGroups().size() == 1) {
            throw new CustomException("The user must have at least one role!");
        }

        Group group = groupService.findGroupByName(roleToAdd.getRole());

        acc.getUserGroups().remove(group);

        return saveAccount(acc);
    }

    @Transactional
    public Account lockUnlockAccount(Account acc, String operation) {
        if(acc.getUserGroups().stream().map(Group::getName).anyMatch("ROLE_ADMINISTRATOR"::equalsIgnoreCase)) {
            throw new CustomException("Can't lock the ADMINISTRATOR!");
        }
        EventName action = EventName.LOCK_USER;
        Account receivedAccount = accountRepository.findAccountByEmailIgnoreCase(acc.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if (operation.equalsIgnoreCase("UNLOCK")) {
            receivedAccount.setLocked(false);

        }
        if (operation.equalsIgnoreCase("LOCK")) {
            receivedAccount.setLocked(true);

        }

        return accountRepository.save(receivedAccount);
    }

}