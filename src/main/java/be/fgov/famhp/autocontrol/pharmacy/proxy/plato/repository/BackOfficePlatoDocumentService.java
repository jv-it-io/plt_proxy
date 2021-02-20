package be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository;

import be.fgov.famhp.plato.backoffice.domain.PharmacyDocument;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

@Service
public class BackOfficePlatoDocumentService implements BackOfficePlatoDocumentFileService<PharmacyDocument> {
    private final GridFsTemplate gridFsTemplate;

    public BackOfficePlatoDocumentService(@Qualifier("gridFsTemplate") GridFsTemplate template) {
        this.gridFsTemplate = template;
    }

    @Override
    public Optional<GridFsResource> find(String id) {
        ObjectId objectId = new ObjectId(id);
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        if(file != null){
            return Optional.ofNullable(gridFsTemplate.getResource(file));
        }
        return Optional.empty();
    }

    @Override
    public ObjectId store(File file, String filename) throws FileNotFoundException {
        ObjectId id = this.gridFsTemplate.store(new FileInputStream(file), filename);
        return id;
    }
    @Override
    public ObjectId storeInputStream(InputStream file, String filename, String contentType) throws FileNotFoundException {
        ObjectId id = this.gridFsTemplate.store(file, filename, contentType);
        return id;
    }
}
