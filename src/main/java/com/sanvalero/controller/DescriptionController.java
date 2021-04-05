package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class DescriptionController implements Initializable {

    public WebView wbFlagImage = new WebView();
    public Label lbName, lbCapital, lbRegion, lbPopulation;
    public Button btIncrease, btReduce;

    private final Country country;
    private final double DEFAULT_ZOOM = 0.4;
    private double zoomModify = DEFAULT_ZOOM;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lbName.setText(country.getName());
        lbCapital.setText(country.getCapital());
        lbRegion.setText(country.getRegion());
        lbPopulation.setText(String.valueOf(country.getPopulation()));
        wbFlagImage.getEngine().load(country.getFlag());
        wbFlagImage.setZoom(DEFAULT_ZOOM);
    }

    public DescriptionController(Country country) {
        this.country = country;
    }


    @FXML
    public void increase(Event event) {
        zoomModify += 0.1;
        wbFlagImage.setZoom(zoomModify);
    }


    @FXML
    public void reduce(Event event) {
        zoomModify -= 0.1;
        wbFlagImage.setZoom(zoomModify);
    }
}
