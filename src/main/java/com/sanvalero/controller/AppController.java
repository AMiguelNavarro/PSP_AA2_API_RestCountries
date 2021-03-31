package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import com.sanvalero.service.CountriesService;
import com.sanvalero.utils.Alerts;
import com.sanvalero.utils.Constants;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.Observable;
import rx.schedulers.Schedulers;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class AppController implements Initializable {

    public Button btFindByName, btViewDescription, btRegions, btExport;
    public TextField tfName, tfNameResult, tfCapitalResult;
    public ListView<Country> lvAllCountries, lvByName, lvAllFromRegion;
    public Label lbNameSearched, lbRegionSelected;
    public ProgressIndicator piCountryFromRegion;
    public ComboBox<String> cbRegions, cbExport;

    private CountriesService apiService;

    private ObservableList<Country> listAllCountries;
    private ObservableList<Country> listCountriesFromRegion;
    private String[] listRegions = new String[] {Constants.REGION_AFRICA, Constants.REGION_AMERICAS, Constants.REGION_ASIA, Constants.REGION_EUROPE, Constants.REGION_OCEANIA};
    private String[] listExportOptions = new String[] {Constants.CSV, Constants.ZIP};
    private Country countrySelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();

        visibleProgressIndicator(false);

        listAllCountries = FXCollections.observableArrayList();
        lvAllCountries.setItems(listAllCountries);
        getAllCountries();

        listCountriesFromRegion = FXCollections.observableArrayList();
        lvAllFromRegion.setItems(listCountriesFromRegion);

        cbRegions.setItems(FXCollections.observableArrayList(listRegions));
        cbRegions.setValue(Constants.DEFAULT_REGION);

        cbExport.setItems(FXCollections.observableArrayList(listExportOptions));
        cbExport.setValue(Constants.CSV);
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

        List<Country> listFilterByName = new ArrayList<>();
        for(Country country : listAllCountries) {
            if (country.getName().contains(name)) {
                listFilterByName.add(country);
            }
        }
        lvByName.setItems(FXCollections.observableList(listFilterByName));

        if (listFilterByName.isEmpty()) {
            Alerts.showInfoAlert("No hay ningún país con las letras: " + name);
        }
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
     * Hace la petición a la API según el continente que se haya seleccionado en el comboBox
     * @param event
     */
    @FXML
    public void findAllFromRegion(Event event) {

        lvAllFromRegion.getItems().clear();
        visibleProgressIndicator(true);
        piCountryFromRegion.setProgress(-1);

        String region = cbRegions.getSelectionModel().getSelectedItem();
        if (region == null || region.equals(Constants.DEFAULT_REGION)) {
            Alerts.showErrorAlert("Debes seleccionar un continente");
            visibleProgressIndicator(false);
            return;
        }

        lbRegionSelected.setText(region);


        apiService.getCountriesFromRegion(region)
                .flatMap(Observable::from)
                .doOnCompleted(() -> {
                    System.out.println("Listado descargado");
                    visibleProgressIndicator(false);
                })
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> listCountriesFromRegion.add(country));


    }


    @FXML
    public void export(Event event) {

        String exportType = cbExport.getSelectionModel().getSelectedItem();

        if (exportType.equals(Constants.CSV)) {
//            CompletableFuture.runAsync(this::exportCSV)
//            .whenComplete((string, throwable) -> System.out.println("OK"));
            if (exportCSV() != null) {
                Alerts.showInfoAlert("Se ha exportado el archivo CSV correctamente");
            }
        }

        if (exportType.equals(Constants.ZIP)) {
            CompletableFuture.supplyAsync(this::exportCSV).thenAccept(this::exportZIP);

//            File file = exportCSV();
//            exportZIP(file);


        }

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
    public void getCountryListViewRegion(Event event) {
        countrySelected = lvAllFromRegion.getSelectionModel().getSelectedItem();

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


    /**
     * Oculta el progressIndicator
     * @param visible
     */
    private void visibleProgressIndicator(boolean visible) {
        piCountryFromRegion.setVisible(visible);
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


    private File exportCSV() {
        File file = null;
        try {
            FileChooser fileChooser = new FileChooser();
            file = fileChooser.showSaveDialog(null);
            FileWriter fileWriter = new FileWriter(file + ".csv");
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("País", "Capital", "Continente", "Población"));

            List<Country> countriesExportList = listAllCountries;

            for(Country country : countriesExportList) {
                printer.printRecord(
                        country.getName(),
                        country.getCapital(),
                        country.getRegion(),
                        country.getPopulation()
                );
            }

            printer.close();

        } catch (IOException e) {
            Alerts.showErrorAlert("Error al exportar los datos en CSV");
        }

        return file;

    }

    private void exportZIP(File file) {

        try {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().concat(".zip"));
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath().concat(".csv"));
            ZipEntry zipEntry = new ZipEntry(file.getName().concat(".csv"));

            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >=0){
                zos.write(bytes, 0, length);
            }
            zos.close();
            fis.close();
            fos.close();

        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }

    }

}
