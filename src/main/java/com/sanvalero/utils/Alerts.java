package com.sanvalero.utils;

import javafx.scene.control.Alert;

public class Alerts {

    /**
     * Muestra una alerta de información
     * @param message
     */
    public static void showInfoAlert(String message) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Información");
        infoAlert.setContentText(message);
        infoAlert.showAndWait();
    }


    /**
     * Muestra una alerta de error
     * @param message
     */
    public static void showErrorAlert(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

}
