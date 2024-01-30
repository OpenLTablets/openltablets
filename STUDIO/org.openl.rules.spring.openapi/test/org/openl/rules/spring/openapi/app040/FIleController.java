package org.openl.rules.spring.openapi.app040;

import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FIleController {

    @PostMapping(value = "/upload/bulk", consumes = "multipart/form-data")
    ResponseEntity<Void> bulkUpload(@RequestPart("files") List<MultipartFile> multipartFiles) {
        return null;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file) {
        return null;
    }

    @PostMapping(value = "/form/{name}", consumes = "multipart/form-data")
    @Parameter(name = "configuration", description = "Pam-Pam")
    public ResponseEntity<Void> form(@CustomPathVariable("name") final String name,
            @RequestPart(value = "configuration") final String configuration,
            @RequestPart(value = "file") final MultipartFile file) {
        return null;
    }

    @PostMapping(value = "/upload", consumes = "application/zip")
    public ResponseEntity<Void> upload(HttpEntity<InputStreamResource> zipFile) {
        return null;
    }

}
