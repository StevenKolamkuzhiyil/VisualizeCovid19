package com.stevenkolamkuzhiyil.Covid19.constroller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("api")
public class ApiController {

    @GetMapping(path = "geojson")
    public void getGeoJson(HttpServletResponse response) {
        response.setContentType("text/plain");
        response.addHeader("Cache-Control", "max-age=60, must-revalidate, no-transform");
        try {
            File initialFile = new File("src/main/resources/static/data/out_map.geojson");
            InputStream in = new FileInputStream(initialFile);
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOError writing file to output stream");
        }

    }

}
