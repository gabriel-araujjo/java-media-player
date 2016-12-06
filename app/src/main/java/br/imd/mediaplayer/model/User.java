package br.imd.mediaplayer.model;

import br.imd.mediaplayer.model.annotation.Model;
import br.imd.mediaplayer.model.annotation.Persist;

import java.util.Arrays;

/**
 * Created by gabriel on 02/12/16.
 */
@Model("usuarios")
public class User extends AbstractModel {

    @Persist
    private String username;

    @Persist
    private String password;

    @Persist
    private String[] roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public boolean addRole(String role) {
        if (role == null) return false;
        if (roles == null) roles = new String[] {role};
        if (Arrays.stream(roles).anyMatch(s -> s.equals(role))) {
            return false;
        }
        roles = Arrays.copyOf(roles, roles.length + 1);
        roles[roles.length] = role;
        return true;
    }

    public boolean removeRole(String role) {
        if (role == null) return false;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(role)) {
                System.arraycopy(roles, i+1, roles, i, roles.length - i - 1);
                roles = Arrays.copyOf(roles, roles.length - 1);
                return true;
            }
        }
        return false;
    }

    public boolean can(String role) {
        return Arrays.stream(roles).anyMatch(s -> s.equals(role));
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}
