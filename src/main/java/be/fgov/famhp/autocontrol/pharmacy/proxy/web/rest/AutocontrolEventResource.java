package be.fgov.famhp.autocontrol.pharmacy.proxy.web.rest;

import be.fgov.famhp.autocontrol.pharmacy.proxy.service.AutocontrolEventService;
import be.fgov.famhp.autocontrol.pharmacy.resttemplate.model.EventListDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AutocontrolEventResource {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    private final AutocontrolEventService autocontrolEventService;

    public AutocontrolEventResource(AutocontrolEventService autocontrolEventService) {
        this.autocontrolEventService = autocontrolEventService;
    }

    @GetMapping(value = "/test")
    public EventListDto getEvents() {
        EventListDto eventListDto = autocontrolEventService.testGetEvent();
        return eventListDto;
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
//        return new ResponseEntity<>(eventListDto, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/batch/launch")
    public void runJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(job, jobParameters);
    }

}
