package com.insightpdf.controller;
import com.insightpdf.model.Document;
import com.insightpdf.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository repo;
    private final RestTemplate rest = new RestTemplate();

    @Value("${processor.url:http://processor:5000/process}")
    private String processorUrl;

    public DocumentController(DocumentRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "patterns", required = false) String patterns) throws Exception {
        String uploadsDir = "/data/uploads";
        new File(uploadsDir).mkdirs();
        String fname = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(uploadsDir, fname);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(file.getBytes());
        }

        Document d = new Document();
        d.setFilename(file.getOriginalFilename());
        d.setInputPath(dest.getAbsolutePath());
        d.setStatus("processing");
        repo.save(d);

        // send to processor
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(dest));
        if (patterns != null) body.add("patterns", patterns);

        HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rest.postForEntity(processorUrl, requestEntity, Map.class);

        if (resp.getStatusCode().is2xxSuccessful()) {
            Map<?,?> map = resp.getBody();
            d.setOutputPath((String)map.get("output"));
            d.setStatus("done");
            d.setProcessedAt(Instant.now());
            d.setProcessingTime(((Number)map.getOrDefault("processing_time", 0)).doubleValue());
            d.setRedactedCount(((Number)map.getOrDefault("redacted_count", 0)).intValue());
            repo.save(d);
            return ResponseEntity.ok(d);
        } else {
            d.setStatus("failed");
            repo.save(d);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("processing failed");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
