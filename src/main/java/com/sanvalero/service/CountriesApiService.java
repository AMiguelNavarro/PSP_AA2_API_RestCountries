package com.sanvalero.service;

import com.sanvalero.domain.Country;
import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

import java.util.List;

public interface CountriesApiService {

    @GET("/rest/v2/all")
    Observable<List<Country>> getAllCountries();

    //Todos los de la Unión Europea
    //https://restcountries.eu/rest/v2/regionalbloc/eu
    @GET("/rest/v2/regionalbloc/eu")
    Observable<List<Country>> getAllCountriesFromUE();

}
