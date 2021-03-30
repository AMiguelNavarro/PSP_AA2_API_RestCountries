package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import com.sanvalero.service.CountriesService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class AppController implements Initializable {

    public Button btAllCountries, btFindByName, btViewDescription, btAllFromUE;
    public TextField tfName, tfNameResult, tfCapitalResult;
    public ListView<Country> lvAllCountries, lvByName, lvAllFromUE;
    public Label lbNameSearched;
    public ProgressIndicator pgAllCountries, pgAllFromUE;

    private CountriesService apiService;

    private ObservableList<Country> listAllCountries;
    private ObservableList<Country> listCountriesFromUE;
    private Country countrySelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();

        pgAllCountries.setDisable(true);
        pgAllFromUE.setDisable(true);

        listAllCountries = FXCollections.observableArrayList();
        lvAllCountries.setItems(listAllCountries);

        listCountriesFromUE = FXCollections.observableArrayList();
        lvAllFromUE.setItems(listCountriesFromUE);
    }


    /**
     * Hace la petición a la API y lista todos los paises dentro de lvAllCountries
     * @param event
     */
    @FXML
    public void getAllCountries(Event event) {

        lvAllCountries.getItems().clear();

        apiService.getAllCountries()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Listado descargado"))
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> listAllCountries.add(country));

    }


    /**
     * Hace la petición a la API según el nombre de país que se introduzca dentro del textfield
     * OJO!! La idea es hacer la petición total y manejarlo con streams
     * @param event
     */
    @FXML
    public void findByName(Event event) {

    }


    /**
     * Carga otra ventana con la ficha descriptiva del país seleccionado
     * @param event
     */
    @FXML
    public void viewDescription(Event event) {

    }


    /**
     * Hace la petición a la API de buscar todos los países de la Unión Europea
     * @param event
     */
    @FXML
    public void findAllFromUE(Event event) {

        lvAllFromUE.getItems().clear();

        apiService.getAllCountriesFromUE()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Listado descargado"))
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> listCountriesFromUE.add(country));

    }


    @FXML
    public void getCountryListViewAll(Event event) {
        countrySelected = lvAllCountries.getSelectionModel().getSelectedItem();

        if (countrySelected == null) {
            // TODO mostrar alerta
        }

        loadCountryOnTextFields(countrySelected);

    }


    @FXML
    public void getCountryListViewUE(Event event) {
        countrySelected = lvAllFromUE.getSelectionModel().getSelectedItem();

        if (countrySelected == null) {
            //TODO mostrar alerta
        }

        loadCountryOnTextFields(countrySelected);
    }



    /*-------------------------------------------------------------------------------------------*/

    public void loadCountryOnTextFields(Country country) {
        tfNameResult.setText(country.getName());
        tfCapitalResult.setText(country.getCapital());
    }

}
