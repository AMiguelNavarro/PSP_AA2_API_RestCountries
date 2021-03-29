package com.sanvalero.utils;

import java.io.File;
import java.net.URL;

public class R {

    /**
     * Acceso a la carpeta interface para cargar las vistas en fxml
     * @param name
     * @return
     */
    public static URL getUI(String name) {
        return Thread.currentThread().getContextClassLoader().getResource("ui" + File.separator + name + ".fxml");
    }

}
