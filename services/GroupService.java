package account.services;

import account.exceptions.MoreThanOneRoleException;
import account.exceptions.NoSuchRoleException;
import account.model.Group;
import account.repositories.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private GroupRepository groupRepository;

    public GroupService (GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
        createRoles();
    }

    public void createRoles() {
        try {
            if(groupRepository.findGroupByNameContains("ROLE_ADMINISTRATOR").isEmpty()) {
                groupRepository.save(new Group("ROLE_ADMINISTRATOR"));
            }
            if(groupRepository.findGroupByNameContains("ROLE_ACCOUNTANT").isEmpty()) {
                groupRepository.save(new Group("ROLE_ACCOUNTANT"));
            }
            if(groupRepository.findGroupByNameContains("ROLE_USER").isEmpty()) {
                groupRepository.save(new Group("ROLE_USER"));
            }
            if(groupRepository.findGroupByNameContains("ROLE_AUDITOR").isEmpty()) {
                groupRepository.save(new Group("ROLE_AUDITOR"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Group findGroupByName(String name) {
        if(groupRepository.findGroupByNameContains(name).size() == 1) {
            return groupRepository.findGroupByNameContains(name).get(0);
        } else if (groupRepository.findGroupByNameContains(name).size() > 1) {
            throw new MoreThanOneRoleException();
        }
        throw new NoSuchRoleException();
    }

}
