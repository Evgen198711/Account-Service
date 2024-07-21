package account.model;

;

public class ChangeRoleDTO {
    private String user;
    private String role;
    private String rights;
    private String operation;

    public ChangeRoleDTO() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String operation) {
        this.user = operation;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        updateRights();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public void updateRights() {

        if (this.role.equalsIgnoreCase("ADMINISTRATOR")) {
            this.rights = "administrative";
        } else {
            this.rights = "business";

        }

    }
}
