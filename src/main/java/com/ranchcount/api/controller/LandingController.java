package com.ranchcount.api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Hidden
@Controller
public class LandingController {

    private static final MediaType APK_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.android.package-archive");

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> landing() {
        ClassPathResource html = new ClassPathResource("static/index.html");
        if (!html.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }

    @GetMapping(value = {"/apk/ranchcount.apk", "/descargar"})
    public ResponseEntity<Resource> descargarApk() throws IOException {
        ClassPathResource apk = new ClassPathResource("static/apk/ranchcount.apk");
        if (!apk.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APK_MEDIA_TYPE);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("Zootecnia-RanchCount.apk").build()
        );
        headers.setContentLength(apk.contentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(apk);
    }
}
