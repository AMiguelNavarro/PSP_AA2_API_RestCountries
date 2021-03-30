package com.sanvalero.service;

import com.sanvalero.domain.Country;
import com.sanvalero.utils.Constants;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import java.util.List;

public class CountriesService {

    private CountriesApiService apiService;

    /**
     * Constructor que inicializa la librería de Retrofit
     */
    public CountriesService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        apiService = retrofit.create(CountriesApiService.class);
    }

    public Observable<List<Country>> getAllCountries() {
        return apiService.getAllCountries();
    }

    public Observable<List<Country>> getCountriesFromRegion(String region) {
        return apiService.getCountriesFromRegion(region);
    }
}
