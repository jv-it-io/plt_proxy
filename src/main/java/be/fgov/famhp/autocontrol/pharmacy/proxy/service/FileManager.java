package be.fgov.famhp.autocontrol.pharmacy.proxy.service;

import be.fgov.fagg.common.VersionedEntity;
import be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository.BackOfficePlatoDocumentFileService;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static org.springframework.core.GenericTypeResolver.getTypeVariableMap;

/**
 * Created by jnn on 27-01-21
 */
@Service
public class FileManager {

    private final Map<String, BackOfficePlatoDocumentFileService> backOfficePlatoDocumentFileServiceMap;

    public FileManager(Map<String, BackOfficePlatoDocumentFileService> backOfficePlatoDocumentFileServiceMap) {
        this.backOfficePlatoDocumentFileServiceMap = backOfficePlatoDocumentFileServiceMap;
    }

    public Optional<GridFsResource> getFile(String id, Class<? extends VersionedEntity> entityClass){
        Optional<BackOfficePlatoDocumentFileService> backOfficePlatoDocumentFileService = backOfficePlatoDocumentFileServiceMap.values().stream()
            .filter(mpmDomainReceiver -> getTypeVariableMap(mpmDomainReceiver.getClass()).values().contains(entityClass))
            .findFirst();

        if(backOfficePlatoDocumentFileService.isPresent()){
            return backOfficePlatoDocumentFileService.get().find(id);

        }
        return Optional.empty();
    }
}
