package com.sanvalero.controller;

import com.sanvalero.domain.Country;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class DescriptionController implements Initializable {

    public WebView wbFlagImage = new WebView();
    public Label lbName, lbCapital, lbRegion;

    private Country country;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lbName.setText(country.getName());
        lbCapital.setText(country.getCapital());
        lbRegion.setText(country.getRegion());
        wbFlagImage.getEngine().load(country.getFlag());
        wbFlagImage.setZoom(0.4);
    }

    public DescriptionController(Country country) {
        this.country = country;
    }
}
