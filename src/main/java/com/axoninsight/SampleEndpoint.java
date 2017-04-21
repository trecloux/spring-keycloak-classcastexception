package com.axoninsight;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SampleEndpoint {

    @GetMapping
    public Map<String,?> root() {
        Map<String, String> out = new HashMap<>();
        out.put("Hello", "World");
        return out;

    }
}
