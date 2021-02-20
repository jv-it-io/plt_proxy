package be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository;

import be.fgov.famhp.plato.backoffice.domain.InspectionDocument;
import be.fgov.famhp.plato.backoffice.domain.InspectionReport;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by jnn on 27-01-21
 */
@Service
public class BackOfficePlatoReportService implements BackOfficePlatoDocumentFileService<InspectionReport> {
    private final GridFsTemplate gridFsTemplate;

    public BackOfficePlatoReportService(@Qualifier("gridFsTemplateReport") GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
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
