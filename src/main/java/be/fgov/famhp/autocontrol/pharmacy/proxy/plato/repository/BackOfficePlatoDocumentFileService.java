package be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository;

import be.fgov.fagg.common.VersionedEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by jnn on 27-01-21
 */
public interface BackOfficePlatoDocumentFileService<E extends VersionedEntity> {
    Optional<GridFsResource> find(String id);
    ObjectId store(File file, String filename) throws FileNotFoundException;
    ObjectId storeInputStream(InputStream file, String filename, String contentType) throws FileNotFoundException;
}
