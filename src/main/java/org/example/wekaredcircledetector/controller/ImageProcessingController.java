package org.example.wekaredcircledetector.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.wekaredcircledetector.service.ImageProcessingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/image")
public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image processed and saved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid image file", content = @Content)
    })
    @PostMapping(value = "/generateModel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateModel(@RequestParam("file") MultipartFile file) {
        try {
            imageProcessingService.generateModel(file);
            return ResponseEntity.ok("Model generated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }

    @PostMapping(value = "/detectRedCircles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> detectRedCircles(@RequestParam("file") MultipartFile file) {
        try {
            imageProcessingService.detectRedCircles(file);
            return ResponseEntity.ok("Model generated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
}