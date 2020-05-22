package com.stevenkolamkuzhiyil.Covid19.constroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {

    @GetMapping(path = {"", "home"})
    public String home() {
        return "index";
    }

}
