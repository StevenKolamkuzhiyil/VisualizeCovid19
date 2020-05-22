package com.stevenkolamkuzhiyil.Covid19.config;

import com.stevenkolamkuzhiyil.Covid19.exception.OutdatedDataException;
import com.stevenkolamkuzhiyil.Covid19.util.ParseGeoJson;
import com.stevenkolamkuzhiyil.Covid19.util.ReadCSV;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@EnableScheduling
@Configuration
public class UpdateGeoJsonScheduler {

    /**
     * @brief A scheduler which fetches COVID-19 data and updates properties of each country in the geojson file.
     * @details Fetch COVID-19 data from 'https://opendata.ecdc.europa.eu/covid19/casedistribution/csv' and
     * filter the data by newest date. Then update the properties of each country in the geojson file.
     * This process is scheduled to run at 4:00 pm every day.
     */
    // cron = seconds, minutes, hours, day of month, month, day of week
    @Scheduled(cron = "0 0 16 * * ?")
    public void updateGeoJson() {
        final String url = "https://opendata.ecdc.europa.eu/covid19/casedistribution/csv";
        final String file = System.getProperty("user.dir") + "\\src\\main\\resources\\templates\\map.geojson";
        final String outfile = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\data\\out_map.geojson";
        final List<String> header = List.of("dateRep", "day", "month", "year", "cases", "deaths", "countriesAndTerritories", "geoId", "countryterritoryCode", "popData2018", "continentExp");

        try {
            ReadCSV readCSV = new ReadCSV(header, url);
            List<Map<String, Object>> records = readCSV.read();
            if (records.isEmpty()) return;
            ParseGeoJson parseGeoJson = new ParseGeoJson(file);
            parseGeoJson.addProperties(records, "geoId");
            parseGeoJson.save(outfile);
        } catch (IOException | OutdatedDataException e) {
            e.printStackTrace();
        }
    }

}
