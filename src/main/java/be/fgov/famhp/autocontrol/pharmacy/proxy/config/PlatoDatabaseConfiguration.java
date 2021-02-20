package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import com.mongodb.MongoClientOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "be.fgov.famhp.autocontrol.pharmacy.proxy.plato.repository",
    mongoTemplateRef = "mongoTemplatePlato"
)
public class PlatoDatabaseConfiguration {

    private final MongoProperties mongoBackofficeProperties;
    private final Environment environment;

    public PlatoDatabaseConfiguration(Environment environment, AutocontrolPharmacyProperties autocontrolPharmacyProperties) {
        this.environment = environment;
        this.mongoBackofficeProperties = autocontrolPharmacyProperties.getPlatoBackoffice();
    }


    @Bean(name = "mongoTemplatePlato")
    public MongoTemplate mongoTemplatePlato(@Qualifier("mongoPlatoFactory") MongoDbFactory mongoPlatoFactory) {
        return new MongoTemplate(mongoPlatoFactory);
    }


    @Bean("mongoPlatoFactory")
    public MongoDbFactory mongoPlatoFactory()  {
        return new SimpleMongoDbFactory(new MongoClientFactory(mongoBackofficeProperties,environment).
            createMongoClient(MongoClientOptions.builder().build()),
            mongoBackofficeProperties.getDatabase());
    }


    @Bean
    @Qualifier("gridFsTemplate")
    public GridFsTemplate gridFsTemplate(@Qualifier("mongoPlatoFactory") MongoDbFactory mongoPlatoFactory, MongoConverter mongoConverter) {
        return new GridFsTemplate(mongoPlatoFactory, mongoConverter);
    }

    @Bean
    @Qualifier("gridFsTemplateReport")
    public GridFsTemplate gridFsTemplateReport(@Qualifier("mongoPlatoFactory")MongoDbFactory mongoPlatoFactory, MongoConverter mongoConverter) {
        return new GridFsTemplate(mongoPlatoFactory, mongoConverter, "REPORTS");
    }
}
