package com.stevenkolamkuzhiyil.Covid19.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ParseGeoJson {

    /**
     * The parsed geojson file.
     */
    private FeatureCollection featureCollection;

    /**
     * @param file The file path as string.
     * @brief Create an object which stores a parsed geojson file.
     * @details Create a FeatureCollection from a geojson file.
     */
    public ParseGeoJson(String file) {
        try (InputStream inputStream = new FileInputStream(new File(file))) {
            this.featureCollection = new ObjectMapper().readValue(inputStream, FeatureCollection.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param outfile The output path.
     * @brief Save the FeatureCollection at specified output path.
     * @details Create or overwrite a file at the specified path and save the FeatureCollection as json.
     */
    public void save(String outfile) {
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(outfile))) {
            String json = new ObjectMapper().writeValueAsString(featureCollection);
            wr.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param data  The list of properties. Each property is a list of key value pairs.
     * @param idKey The key to decide whether a property entry belongs to a feature entry.
     * @brief Add properties to an entry of the feature collection.
     * @details Update the properties of each feature where the feature contains a property 'idKey' which matches
     * The 'idKey' of a property entry.
     */
    public void addProperties(List<Map<String, Object>> data, String idKey) {
        List<Feature> featureList = this.featureCollection.getFeatures();
        featureList.forEach(f -> {
            String id = (String) f.getProperties().get(idKey);
            Map<String, Object> record = data.stream()
                    .filter(r -> id.equals(r.get(idKey)))
                    .findFirst()
                    .orElse(null);
            if (record == null) return;

            f.setProperties(record);
        });

        featureCollection.setFeatures(featureList);
    }

}
