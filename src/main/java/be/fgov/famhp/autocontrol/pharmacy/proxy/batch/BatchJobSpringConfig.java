package be.fgov.famhp.autocontrol.pharmacy.proxy.batch;

import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.AnnexeDocumentAddedProcessor;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.CapaUpdatedProcessor;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.FormCopyCompletedProcessor;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.ItemProcessorEvent;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.NewFormCompletedProcessor;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.processor.ProcessorEventsService;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.reader.EventItemReader;
import be.fgov.famhp.autocontrol.pharmacy.proxy.batch.writer.EventItemWriter;
import be.fgov.famhp.autocontrol.pharmacy.proxy.dto.CapaUpdatedEventDto;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableBatchProcessing
@ConditionalOnProperty("autocontrol.batch.enabled")
public class BatchJobSpringConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final DataSource dataSource;

    private final AnnexeDocumentAddedProcessor annexeDocumentAddedProcessor;

    private final NewFormCompletedProcessor newFormCompletedProcessor;

    private final FormCopyCompletedProcessor formCopyCompletedProcessor;

    private final CapaUpdatedProcessor capaUpdatedProcessor;

    public BatchJobSpringConfig(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        DataSource dataSource,
        AnnexeDocumentAddedProcessor annexeDocumentAddedProcessor,
        NewFormCompletedProcessor newFormCompletedProcessor,
        FormCopyCompletedProcessor formCopyCompletedProcessor,
        CapaUpdatedProcessor capaUpdatedProcessor) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
        this.annexeDocumentAddedProcessor = annexeDocumentAddedProcessor;
        this.newFormCompletedProcessor = newFormCompletedProcessor;
        this.formCopyCompletedProcessor = formCopyCompletedProcessor;
        this.capaUpdatedProcessor = capaUpdatedProcessor;
    }

    @Bean
    @StepScope
    public ItemReader<EventDto> itemReader() {
        return new EventItemReader();
    }

    @Bean
    public ItemWriter<EventDto> itemWriter() {
        return new EventItemWriter();
    }

    @Bean
    protected Step step1() {
        return stepBuilderFactory.get("step1")
            .<EventDto, EventDto>chunk(10)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    @Bean
    public ItemProcessor<? super EventDto, ? extends EventDto> itemProcessor() {
        HashMap<String, ProcessorEventsService> map = new HashMap<>();
        map.put("ANNEX_DOCUMENT_ADDED", annexeDocumentAddedProcessor);
        map.put("NEW_FORM_COMPLETED", newFormCompletedProcessor);
        map.put("FORMCOPY_COMPLETED", formCopyCompletedProcessor);
        map.put("CAPA_UPDATED", capaUpdatedProcessor);
        return new ItemProcessorEvent(map);
    }

    @Bean
    public Job job() {
        return jobBuilderFactory
            .get("autocontrolEventsJob")
            .incrementer(new RunIdIncrementer())
            .start(step1())
            .build();
    }

}
