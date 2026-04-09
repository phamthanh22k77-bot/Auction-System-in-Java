package com.example.login;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField tname;

    @FXML
    private PasswordField tpass;

    @FXML
    private Button btnCon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}