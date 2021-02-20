package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@TestConfiguration
@EnableMongoRepositories(value = "be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository", mongoTemplateRef = "mongoTemplatePlato")
public class MongoDatabaseConfigurationTest {

    @Bean(name = "mongoTemplatePlato")
    public MongoTemplate mongoTemplatePlato(MongoClient mongoClient, AutocontrolPharmacyProperties autocontrolPharmacyProperties) throws Exception {
        return new MongoTemplate(mongoClient,autocontrolPharmacyProperties.platoBackoffice.getDatabase());   //trick to use the embedded mongo client for the test.
    }
}
