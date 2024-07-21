package account.model;

import java.util.List;

public class AccountWithRolesDTO {
    private  long id;
    private  String name;
    private  String lastname;
    private  String email;
    private  List<String> roles;

    public AccountWithRolesDTO() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public AccountWithRolesDTO(Builder acc) {
        this.id = acc.id;
        this.name = acc.name;
        this.lastname = acc.lastname;
        this.email = acc.email;
        this.roles = acc.roles;
    }



    public static class Builder {
        private long id;
        private String name;
        private String lastname;
        private String email;
        private List<String> roles;

        public Builder() {
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setRoles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public AccountWithRolesDTO build() {
            return new AccountWithRolesDTO(this);
        }
    }
}
