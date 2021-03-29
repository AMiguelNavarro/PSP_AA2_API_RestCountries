package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import com.sanvalero.service.CountriesService;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class AppController implements Initializable {

    public Button btAllCountries, btFindByName, btViewDescription, btAllFromUE;
    public TextField tfName, tfNameResult, tfCapitalResult;
    public ListView lvAllCountries, lvByName, lvAllFromUE;
    public Label lbNameSearched;
    public ProgressIndicator pgAllCountries, pgAllFromUE;

    public CountriesService apiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();
    }



    @FXML
    public void getAllCountries(Event event) {

        lvAllCountries.getItems().clear();
        List<Country> listAllCountries = apiService.getAllCountries()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Listado descargado"))
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .toList()
                .toBlocking()
                .first();

        lvAllCountries.setItems(FXCollections.observableList(listAllCountries));


    }


    @FXML
    public void findByName(Event event) {

    }


    @FXML
    public void viewDescription(Event event) {

    }


    @FXML
    public void findAllFromUE(Event event) {

    }

}
