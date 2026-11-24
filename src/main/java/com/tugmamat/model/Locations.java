package com.tugmamat.model;

import java.util.List;

/** Cities and municipalities of Batangas, plus a province-wide option. */
public final class Locations {
    private Locations() {}

    public static final String ALL = "Entire Batangas";

    public static final List<String> LIST = List.of(
            ALL,
            "Batangas City", "Lipa City", "Tanauan City", "Santo Tomas City",
            "Agoncillo", "Alitagtag", "Balayan", "Balete", "Bauan", "Calaca", "Calatagan", "Cuenca",
            "Ibaan", "Laurel", "Lemery", "Lian", "Lobo", "Mabini", "Malvar", "Mataasnakahoy",
            "Nasugbu", "Padre Garcia", "Rosario", "San Jose", "San Juan", "San Luis", "San Nicolas",
            "San Pascual", "Santa Teresita", "Taal", "Talisay", "Taysan", "Tingloy", "Tuy");

    public static boolean isAll(String s) {
        return s != null && (s.trim().equalsIgnoreCase(ALL) || s.trim().equalsIgnoreCase("Batangas"));
    }
}
