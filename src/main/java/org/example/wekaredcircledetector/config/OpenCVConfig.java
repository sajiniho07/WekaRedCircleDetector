package org.example.wekaredcircledetector.config;

import nu.pattern.OpenCV;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCVConfig {
    public OpenCVConfig() {
        OpenCV.loadLocally();
    }

}
