package account.services;

import account.events.Event;
import account.events.EventName;
import account.exceptions.CustomException;
import account.model.Account;
import account.model.Group;
import account.repositories.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final AccountService accountService;
    private final ApplicationEventPublisher publisher;

    public EventService(EventRepository eventRepository,
                        AccountService accountService,
                        ApplicationEventPublisher publisher) {
        this.eventRepository = eventRepository;
        this.accountService = accountService;
        this.publisher = publisher;
    }

    public Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public void publishEvent(EventName eventName, String user, String object) {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String endpointPath = request.getRequestURI();

        publishEvent(eventName, user, object, endpointPath);
    }

    public void publishEvent(EventName eventName, String user, String object, String path) {
        Event eventDb = new Event();
        eventDb.setAction(eventName.name());
        eventDb.setSubject(user);
        eventDb.setObject(object);
        eventDb.setPath(path);

        eventController(eventDb);
    }

    @EventListener
    public void onEvent(Event someEvent) {
        eventRepository.save(someEvent);
    }

    @EventListener
    public void onFailureAuthentication(AuthenticationFailureBadCredentialsEvent event) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String endpointPath = request.getRequestURI();

        String username = (String) event.getAuthentication().getPrincipal();

        publishEvent(EventName.LOGIN_FAILED,
                username,
                endpointPath);

    }

    @EventListener
    public void onAccessSuccess(AuthenticationSuccessEvent event) {
        String subject = event.getAuthentication().getName();
        boolean eventExistInDb = eventRepository.findTheLastRecordForTheUser().isPresent();
        Event eventFromDb = eventRepository.findTheLastRecordForTheUser().get();
        if(eventExistInDb && subject.equalsIgnoreCase(eventFromDb.getSubject())) {
            eventFromDb.setCounter(0);
            eventRepository.save(eventFromDb);
        }
    }

    @EventListener
    public void onAccessDenied(AuthorizationDeniedEvent<?> event) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String endpointPath = request.getRequestURI();

        String username = event.getAuthentication().get().getName();

        publishEvent(EventName.ACCESS_DENIED, username, endpointPath);
    }

    private void eventController(Event event) {
        int counter = 0;

        if (!event.getAction().equalsIgnoreCase(EventName.LOGIN_FAILED.name())) {
            event.setCounter(counter);
            publisher.publishEvent(event);
            return;
        }

        Event received = eventRepository.findTheLastRecordForTheUser()
                .orElseGet(() -> {
                    event.setCounter(-1);
                    return event;
                });

        Optional<Account> acc = accountService.findAccountByEmail(event.getSubject());

        if(acc.isPresent() && acc.get().getUserGroups().stream().map(Group::getName).anyMatch("ROLE_ADMINISTRATOR"::equalsIgnoreCase)) {
            return;
        }

        if (received.getSubject().equalsIgnoreCase(event.getSubject()) &&
                received.getAction().equalsIgnoreCase(EventName.LOGIN_FAILED.name())
                && acc.isPresent()) {
            counter = received.getCounter();
            ++counter;
        }

        event.setCounter(counter);
        publisher.publishEvent(event);


        if (counter == 4) {
            publishEvent(EventName.BRUTE_FORCE,
                    event.getSubject(),
                    event.getObject());

            if (acc.get().getUserGroups().stream().map(Group::getName).noneMatch(s -> s.equalsIgnoreCase("ROLE_ADMINISTRATOR"))) {
                accountService.lockUnlockAccount(acc.get(), "LOCK");

                publishEvent(EventName.LOCK_USER,
                        event.getSubject(),
                        "Lock user %s".formatted(event.getSubject()),
                        "/api/admin/user/access");

            } else {
                throw new CustomException("Can't lock the ADMINISTRATOR!");
            }

        }
    }


}
