package sg.edu.nus.iss.profile_service.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sg.edu.nus.iss.profile_service.model.LatLng;

@Service
public class ExternalLocationService {

    private final RestTemplate restTemplate;

    @Autowired
    public ExternalLocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LatLng getCoordinates(String pincode) {
        String url = "http://localhost:8083/location/coordinates?pincode=" + pincode;
        try {
            return restTemplate.getForObject(url, LatLng.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching coordinates from external service", e);
        }
    }
}
