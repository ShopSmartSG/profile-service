package sg.edu.nus.iss.profile_service.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sg.edu.nus.iss.profile_service.controller.CustomerController;
import sg.edu.nus.iss.profile_service.model.LatLng;

@Service
public class ExternalLocationService {
    @Value("${location.service.url}")
    private String locationServiceUrl;

    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(ExternalLocationService.class);



    @Autowired
    public ExternalLocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LatLng getCoordinates(String pincode) {
        String url = locationServiceUrl+"/location/coordinates?pincode=" + pincode;

        log.info("{\"message\": \"Fetching coordinates from external service: {}\"}", url);
        try {
            return restTemplate.getForObject(url, LatLng.class);
        } catch (Exception e) {
            log.error("{\"message\": \"Error fetching coordinates from external service\"}");
            throw new RuntimeException("Error fetching coordinates from external service", e);
        }
    }
}
