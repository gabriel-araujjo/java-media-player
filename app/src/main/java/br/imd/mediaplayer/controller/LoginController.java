package br.imd.mediaplayer.controller;

import br.imd.mediaplayer.DaoConfig;
import br.imd.mediaplayer.dao.DAO;
import br.imd.mediaplayer.model.User;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.util.Optional;


public class LoginController extends AbstractController {

    private TextField tfEmail;
    private TextField tfPassword;

    public void loginAttempt(ActionEvent actionEvent) {

        String email = tfEmail.getText();

        Optional<User> user = DAO.forModel(User.class).list().stream().filter(u -> u.getUsername().equals(email)).findFirst();

        if (user.isPresent() && user.get().getPassword().equals(tfPassword.getText())) {
            getApp().setLayout("main");
        } else {
            // user is not authenticated
            System.out.println("User not authenticated");
        }


    }

    @Override
    public String getTitle() {
        return super.getTitle() + " - Login";
    }

    @Override
    protected void onAttached() {
        super.onAttached();

        tfEmail = (TextField) getScene().lookup("#tfEmail");
        tfPassword = (TextField) getScene().lookup("#tfPassword");

        if (DaoConfig.FRESH_CONFIG) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword("admin");
            user.setRoles(new String[] {"add_user", "create_playlist", "add_direc"});
            DAO.forModel(User.class).insert(user);
        }
    }
}
