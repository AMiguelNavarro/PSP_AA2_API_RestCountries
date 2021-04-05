package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import com.sanvalero.service.CountriesService;
import com.sanvalero.utils.Alerts;
import com.sanvalero.utils.Constants;
import com.sanvalero.utils.R;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.Observable;
import rx.schedulers.Schedulers;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class AppController implements Initializable {

    public Button btFindByPopulation, btViewDescription, btRegions, btExport;
    public TextField tfPopulation, tfNameResult, tfCapitalResult;
    public ListView<Country> lvAllCountries, lvAllFromRegion;
    public Label lbPopulation, lbRegionSelected;
    public ProgressIndicator piCountryFromRegion, piPopulation;
    public ComboBox<String> cbRegions, cbExport;
    public TableView tvByPopulation;


    private CountriesService apiService;

    private ObservableList<Country> listAllCountries;
    private ObservableList<Country> listCountriesFromRegion;
    private ObservableList<String> regionObservableListComboBox;
    private ObservableList<Country> listFilterByName;
    private String[] listExportOptions = new String[] {Constants.CSV, Constants.ZIP};
    private Country countrySelected;
    private File file;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiService = new CountriesService();

        visibleProgressIndicatorRegion(false);
        visibleProgressIndicatorPopulation(false);

        listFilterByName = FXCollections.observableArrayList();
        tvByPopulation.setItems(listFilterByName);

        listAllCountries = FXCollections.observableArrayList();
        lvAllCountries.setItems(listAllCountries);
        getAllCountries();

        listCountriesFromRegion = FXCollections.observableArrayList();
        lvAllFromRegion.setItems(listCountriesFromRegion);

        regionObservableListComboBox = FXCollections.observableArrayList();
        loadComboBoxRegions();
        cbRegions.setValue(Constants.DEFAULT_REGION);

        cbExport.setItems(FXCollections.observableArrayList(listExportOptions));
        cbExport.setValue(Constants.CSV);

        fixColumns();
    }



    /**
     * Aprovechando la consulta hecha a la API con todos los paises, se añade a una lista y se filtra según
     * el nombre de país que haya escrito el usuario dentro del textfield
     * OJO!! La idea es hacer la petición total y manejarlo con streams
     * @param event
     */
    @FXML
    public void findByPopulation(Event event) {
        tvByPopulation.getItems().clear();
        listFilterByName.clear();
        visibleProgressIndicatorPopulation(true);
        piPopulation.setProgress(-1);

        try {

            int number = Integer.parseInt(tfPopulation.getText());

            lbPopulation.setText(String.valueOf(number));

            apiService.getAllCountries()
                    .flatMap(Observable::from)
                    // Se filtran los datos para encontrar los paises con más población de la indicada
                    .filter(country -> country.getPopulation() > number)
                    //Ordena de menor a mayor los países por población
                    .sorted((a,b) -> Integer.compare(a.getPopulation(), b.getPopulation()))
                    .doOnCompleted(() -> {
                        visibleProgressIndicatorPopulation(false);
                    })
                    .doOnError(throwable ->  {
                        visibleProgressIndicatorPopulation(false);
                    })
                    .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                    .subscribe(country -> {
                        Platform.runLater(() -> {
                            listFilterByName.add(country);
                            //System.out.println("Añadido el país: " + country.getName() + " con " + country.getPopulation() + " habitantes");
                        });
                    });


        } catch (NumberFormatException e) {
            Alerts.showErrorAlert("Introduce un formato de número entero correcto");
            visibleProgressIndicatorPopulation(false);
            tfPopulation.requestFocus();
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
        visibleProgressIndicatorRegion(true);
        piCountryFromRegion.setProgress(-1);

        String region = cbRegions.getSelectionModel().getSelectedItem();
        if (region == null || region.isEmpty() || region.isBlank() || region.equals(Constants.DEFAULT_REGION)) {
            Alerts.showErrorAlert("Debes seleccionar un continente");
            visibleProgressIndicatorRegion(false);
            return;
        }

        lbRegionSelected.setText(region);


        apiService.getCountriesFromRegion(region)
                .flatMap(Observable::from)
                .doOnCompleted(() -> {
                    System.out.println("Listado descargado");
                    visibleProgressIndicatorRegion(false);
                })
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        listCountriesFromRegion.add(country);
                    });
                });


    }


    /**
     * Exporta los datos en CSV o formato ZIP
     * @param event
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @FXML
    public void export(Event event) throws ExecutionException, InterruptedException {

        String exportType = cbExport.getSelectionModel().getSelectedItem();

        // Exportar como CSV
        if (exportType.equals(Constants.CSV)) {

            if (exportCSV() != null) {
                Alerts.showInfoAlert("Se ha exportado el archivo CSV correctamente");
            }

        }

        // Exportar como CSV y comprimir en ZIP
        if (exportType.equals(Constants.ZIP)) {

            file = exportCSV();
            CompletableFuture.supplyAsync(() -> file.getAbsolutePath().concat(".zip"))
                    .thenAccept(System.out::println)
                    .whenComplete((unused, throwable) -> {
                        System.out.println("Archivo .zip generado en: " + file.getAbsolutePath().concat(".zip"));
                        Platform.runLater(() -> {
                            exportZIP(file);
                            Alerts.showInfoAlert("Archivo .zip generado correctamente");
                        });
                    }).get();

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
    public void getCountryTableViewByPopulation(Event event) {
        countrySelected = (Country) tvByPopulation.getSelectionModel().getSelectedItem();

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
     * Oculta el progressIndicator de region
     * @param visible
     */
    private void visibleProgressIndicatorRegion(boolean visible) {
        piCountryFromRegion.setVisible(visible);
    }

    /**
     * Oculta el progressIndicator de population
     * @param visible
     */
    private void visibleProgressIndicatorPopulation(boolean visible) {
        piPopulation.setVisible(visible);
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


    /**
     * Exporta a CSV los datos
     * @return Devuelve un objeto File
     */
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

    /**
     * Convierte un archivo a formato ZIP
     * @param file
     */
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

            // Se borra el archivo csv para evitar duplicidades
            Files.delete(Path.of(file.getAbsolutePath().concat(".csv")));

        } catch (IOException ex) {
            Alerts.showErrorAlert("Error al exportar en formato ZIP");
        }

    }


    /**
     * Carga las regiones de la API dentro del comboBox de continentes de forma automática
     */
    private void loadComboBoxRegions() {

        cbRegions.setItems(regionObservableListComboBox);
        apiService.getAllCountries()
                .flatMap(Observable::from)
                .distinct(Country::getRegion)
                .doOnCompleted(() -> {
                    System.out.println("Se cargan las regiones");
                    visibleProgressIndicatorRegion(false);
                })
                .doOnError(throwable -> System.out.println("ERROR: " + throwable))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        if (country.getRegion().equals("") || country.getRegion().isEmpty() || country.getRegion() == null) {
                            country.setRegion(Constants.DEFAULT_REGION);
                        }
                        regionObservableListComboBox.add(country.getRegion());
                        System.out.println(country.getRegion() + " añadido a la lista");
                    });
                });

    }


    /**
     * Establece la columnas del table view y ajusta su ancho
     */
    private void fixColumns() {
        Field[] fields = Country.class.getDeclaredFields();
        for(Field field : fields) {
            if (field.getName().equals("subregion") || field.getName().equals("flag") || field.getName().equals("region") || field.getName().equals("capital")) {
                continue;
            }

            TableColumn<Country, String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvByPopulation.getColumns().add(column);
        }

        tvByPopulation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

}
