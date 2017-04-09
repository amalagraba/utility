package com.utility.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class IndexController {

    @GetMapping("/")
    public String hello() {
        return "Utility API";
    }
}
