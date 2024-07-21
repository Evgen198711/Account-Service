package account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String name;
    @JsonIgnore
    private String rights;
    @ManyToMany(mappedBy = "roles")
    private Set<Account> users = new HashSet<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
        updateRights();
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        updateRights();
    }

    public String getGroup() {
        return rights;
    }

    public void setGroup(String group) {
        this.rights = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return getName().equals(group.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @PrePersist
    @PreUpdate
    public void updateRights() {

        if (this.name.equalsIgnoreCase("ROLE_ADMINISTRATOR")) {
            this.rights = "administrative";

        } else {
            this.rights = "business";
        }
    }

}
