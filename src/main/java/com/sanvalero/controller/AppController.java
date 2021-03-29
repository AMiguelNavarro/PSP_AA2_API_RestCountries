package com.sanvalero.controller;

import com.sanvalero.service.CountriesService;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    public Button btAllCountries, btFindByName, btViewDescription, btAllFromEEUU;
    public TextField tfName, tfNameResult, tfCapitalResult;
    public ListView lvAllCountries, lvByName, lvAllFromEEUU;
    public Label lbNameSearched;
    public ProgressIndicator pgAllCountries, pgAllFromEEUU;

    public CountriesService apiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();
    }



    @FXML
    public void getAllCountries(Event event) {

    }


    @FXML
    public void findByName(Event event) {

    }


    @FXML
    public void viewDescription(Event event) {

    }


    @FXML
    public void findAllFromEEUU(Event event) {

    }

}
