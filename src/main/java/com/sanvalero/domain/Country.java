package com.sanvalero.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    public String name;
    public String capital;
    public String region;
    public String subRegion;
    public int population;

    @Override
    public String toString() {
        return name + " [ " + capital + region + " ]";
    }
}
