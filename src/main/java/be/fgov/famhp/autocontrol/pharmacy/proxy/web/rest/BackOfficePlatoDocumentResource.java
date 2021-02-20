package be.fgov.famhp.autocontrol.pharmacy.proxy.web.rest;

import be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository.BackOfficePlatoDocumentService;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
public class BackOfficePlatoDocumentResource {
    private final BackOfficePlatoDocumentService backOfficePlatoDocumentService;

    public BackOfficePlatoDocumentResource(BackOfficePlatoDocumentService backOfficePlatoDocumentService) {
        this.backOfficePlatoDocumentService = backOfficePlatoDocumentService;
    }

    @GetMapping("/{id}")
    public GridFsResource find(@PathVariable("id") String id) {
        return backOfficePlatoDocumentService.find(id).orElse(null);
    }

    @PostMapping
    public String store() throws FileNotFoundException {
        return backOfficePlatoDocumentService.store(new File("rzs"),"test").toString();
    }
}
