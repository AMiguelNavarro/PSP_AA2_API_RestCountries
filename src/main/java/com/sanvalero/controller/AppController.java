package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import com.sanvalero.service.CountriesService;
import com.sanvalero.utils.Alerts;
import com.sanvalero.utils.R;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;


public class AppController implements Initializable {

    public Button btFindByName, btViewDescription, btAllFromUE;
    public TextField tfName, tfNameResult, tfCapitalResult;
    public ListView<Country> lvAllCountries, lvByName, lvAllFromUE;
    public Label lbNameSearched;
    public ProgressIndicator pgAllFromUE;

    private CountriesService apiService;

    private ObservableList<Country> listAllCountries;
    private ObservableList<Country> listCountriesFromUE;
    private Country countrySelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();

        disableProgressIndicator(true);

        listAllCountries = FXCollections.observableArrayList();
        lvAllCountries.setItems(listAllCountries);
        getAllCountries();

        listCountriesFromUE = FXCollections.observableArrayList();
        lvAllFromUE.setItems(listCountriesFromUE);
    }


    /**
     * Aprovechando la consulta hecha a la API con todos los paises, se añade a una lista y se filtra según
     * el nombre de país que haya escrito el usuario dentro del textfield
     * OJO!! La idea es hacer la petición total y manejarlo con streams
     * @param event
     */
    @FXML
    public void findByName(Event event) {
        String name = tfName.getText();

        if (name.isEmpty() || name.isBlank()) {
            lvByName.getItems().clear();
            Alerts.showErrorAlert("Debes escribir un nombre de país");
            return;
        }

        lbNameSearched.setText(name);

        List<Country> lista = new ArrayList<>();
        for(Country country : listAllCountries) {
            if (country.getName().contains(name)) {
                lista.add(country);
            }
        }
        lvByName.setItems(FXCollections.observableList(lista));
    }


    /**
     * Carga otra ventana con la ficha descriptiva del país seleccionado
     * @param event
     */
    @FXML
    public void viewDescription(Event event) {

        if (!validateSelectionItem()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            DescriptionController descriptionController = new DescriptionController(countrySelected);

            loader.setLocation(R.getUI("description"));
            loader.setController(descriptionController);

            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);
            stage.show();

        } catch (IOException ioException) {
            Alerts.showErrorAlert("No se ha podido abrir la ficha descriptiva");
        }

    }


    /**
     * Hace la petición a la API de buscar todos los países de la Unión Europea
     * @param event
     */
    @FXML
    public void findAllFromUE(Event event) {

        lvAllFromUE.getItems().clear();
        disableProgressIndicator(false);

        apiService.getAllCountriesFromUE()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Listado descargado"))
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> listCountriesFromUE.add(country));

        disableProgressIndicator(true);

    }


    /**
     * Recoge el item seleccionado del listView de todos los paises y lo añade a los texfields
     * correspondientes
     * @param event
     */
    @FXML
    public void getCountryListViewAll(Event event) {
        countrySelected = lvAllCountries.getSelectionModel().getSelectedItem();

        if (!validateSelectionItem()) {
            return;
        }

        loadCountryOnTextFields(countrySelected);

    }


    /**
     * Recoge el item seleccionado del listView de todos los paises de la UE y lo añade a los texfields
     * correspondientes
     * @param event
     */
    @FXML
    public void getCountryListViewUE(Event event) {
        countrySelected = lvAllFromUE.getSelectionModel().getSelectedItem();

        if (!validateSelectionItem()) {
            return;
        }

        loadCountryOnTextFields(countrySelected);
    }


    /**
     * Recoge el item seleccionado del listView de todos los paises filtrados por nombre y lo añade a los texfields
     * correspondientes
     * @param event
     */
    @FXML
    public void getCountryListViewByName(Event event) {
        countrySelected = lvByName.getSelectionModel().getSelectedItem();

        if (!validateSelectionItem()) {
            return;
        }

        loadCountryOnTextFields(countrySelected);
    }



    /*-------------------------------------------------------------------------------------------*/

    /**
     * Carga el país seleccionado en el ListView dentro de los TextFields
     * @param country
     */
    private void loadCountryOnTextFields(Country country) {
        tfNameResult.setText(country.getName());
        tfCapitalResult.setText(country.getCapital());
    }


    /**
     * Hace la petición a la API y lista todos los paises dentro de lvAllCountries
     *
     */
    private void getAllCountries() {

        lvAllCountries.getItems().clear();

        apiService.getAllCountries()
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Listado descargado"))
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> listAllCountries.add(country));

    }


    private void disableProgressIndicator (boolean disable) {
        pgAllFromUE.setDisable(disable);
    }



    /**
     * Método que comprueba si el objeto countrySelected es nulo.
     * @return Devuelve false en caso de ser nulo y true si no lo es.
     *
     */
    private boolean validateSelectionItem() {
        if (countrySelected == null) {
            Alerts.showErrorAlert("No has seleccionado ningún país");
            return false;
        } else {
            return true;
        }
    }

}
