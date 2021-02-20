package be.fgov.famhp.autocontrol.pharmacy.proxy.service;

import be.fgov.famhp.autocontrol.pharmacy.proxy.AutocontrolPharmacyProxyApp;
import be.fgov.famhp.autocontrol.pharmacy.proxy.config.TestSecurityConfiguration;
import be.fgov.famhp.plato.backoffice.domain.InspectionReport;
import be.fgov.famhp.plato.backoffice.domain.PharmacyDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by jnn on 27-01-21
 */
@SpringBootTest(classes = {AutocontrolPharmacyProxyApp.class,
    TestSecurityConfiguration.class
})
@Disabled
public class FileManagerServiceTest {

    @Autowired
    @Qualifier("gridFsTemplate")
    GridFsTemplate gridFsTemplate;

    @Autowired
    @Qualifier("gridFsTemplateReport")
    GridFsTemplate gridFsTemplateReport;

    @Autowired
    private FileManager fileManager;

    private ObjectId storeId;
    private ObjectId reportStoreId;


    @Test
    public void getPharmacyDocumentTest() throws IOException {

        String filename = "gridfsDocumentTest.xml";
        ClassPathResource resource = new ClassPathResource("gridfs/" + filename);

        this.storeId = gridFsTemplate.store(resource.getInputStream(), filename);

        Optional<GridFsResource> file = fileManager.getFile(this.storeId.toString(), PharmacyDocument.class);
        assertThat("Document should be found in FS bucket", file.isPresent(), equalTo(true));

        Optional<GridFsResource> file2 = fileManager.getFile(this.storeId.toString(), InspectionReport.class);
        assertThat("Document should not be found in REPORT bucket", file2.isPresent(), equalTo(false));
    }

    @Test
    public void getReportDocumentTest() throws IOException {
        String filenameReport = "gridfsDocumentTest2.xml";

        ClassPathResource resourceReport = new ClassPathResource("gridfs/" + filenameReport);
        this.reportStoreId = gridFsTemplateReport.store(resourceReport.getInputStream(), filenameReport);

        Optional<GridFsResource> file = fileManager.getFile(this.reportStoreId.toString(), PharmacyDocument.class);
        assertThat("Document should not be found in FS bucket", file.isPresent(), equalTo(false));

        Optional<GridFsResource> file2 = fileManager.getFile(this.reportStoreId.toString(), InspectionReport.class);
        assertThat("Document should be found in REPORT bucket", file2.isPresent(), equalTo(true));
    }

    @AfterEach
    public  void removeDocument(){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(this.storeId)));
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(this.reportStoreId)));
    }
}
