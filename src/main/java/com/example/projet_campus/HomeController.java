package com.example.projet_campus;

import com.example.projet_campus.classes.User;
import com.example.projet_campus.db.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

public class HomeController {
    @FXML private TextField    usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;

    private Stage stage;

    /** Called by HelloApplication.start() */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter both fields.");
            return;
        }

        try {
            User u = UserDAO.findByUsername(user);
            if (u == null || !BCrypt.checkpw(pass, u.getPasswordHash())) {
                statusLabel.setText("Invalid credentials");
                return;
            }
            openSimulation(u);
        } catch (SQLException | IOException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter both fields.");
            return;
        }

        try {
            String hash = BCrypt.hashpw(pass, BCrypt.gensalt());
            UserDAO.register(user, hash, "USER");
            statusLabel.setText("Registered! You can now log in.");
        } catch (SQLException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openSimulation(User loggedInUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Simulation.fxml")
        );
        Parent root = loader.load();
        SimulationController simCtrl = loader.getController();
        simCtrl.setCurrentUser(loggedInUser);

        stage.setScene(new Scene(root));
        stage.setTitle("Campus Simulator — Simulation");
    }
}
