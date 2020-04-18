package Controllers;

import Client.Client;
import DataClasses.User;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements BaseController, Initializable
{
    private Client client;
    public Button signInButton;
    public Button newUserButton;
    public TextField enterUsername;
    public TextField enterPassword;
    public Label invalidLabel;

    public List<User> users;

    public void onUsernameChanged()
    {
        if(!enterPassword.getText().equals("") && !enterUsername.getText().equals(""))
        {
            signInButton.setDisable(false);
        }
        else
        {
            signInButton.setDisable(true);
        }
    }

    public void onPasswordChanged()
    {
        if(!enterUsername.getText().equals("") && !enterPassword.getText().equals(""))
        {
            signInButton.setDisable(false);
        }
        else
        {
            signInButton.setDisable(true);
        }
    }

    public void onSignInClicked()
    {
        //USE CLIENT OBJECT IN CONTROLLER TO SEND USERNAME AND PASSWORD TO SERVER FOR A QUERY

        users= new ArrayList<>();
        users.add(new User("Jon", "J", "O", "a"));
        users.add(new User("Bas", "J", "O", "b"));
        users.add(new User("Ril", "J", "O", "c"));

        if(contains(enterUsername.getText(), enterPassword.getText()))
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../sample/Menu.fxml"));
                Parent root = loader.load();
                MenuController mc = loader.getController();
                Stage stage = (Stage) newUserButton.getScene().getWindow();
                stage.close();
                stage.setTitle("Menu");
                stage.setScene(new Scene(root));
                stage.show();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            invalidLabel.setText("INVALID CREDENTIALS");
        }

    }

    public void onNewUserClicked()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../sample/Register.fxml"));
            Parent root = loader.load();
            RegisterController rc = loader.getController();
            Stage stage = (Stage) newUserButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Register");
            rc.confirmButton.setText("Register");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    // TEMP: USED TO CHECK USERNAME AND PASSWORD BEFORE WE HAVE CODE TO CONNECT TO A SERVER
    public boolean contains(String username, String pass)
    {
        for(int i = 0; i < users.size(); i++)
        {
            if(username.equals(users.get(i).getUsername()) && pass.equals(users.get(i).getPassword()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(Serializable msg) {

    }
}
