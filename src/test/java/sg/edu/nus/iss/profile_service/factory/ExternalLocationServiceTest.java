package sg.edu.nus.iss.profile_service.factory;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import sg.edu.nus.iss.profile_service.model.LatLng;

public class ExternalLocationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalLocationService externalLocationService;

    @Value("${location.service.url}")
    private String locationServiceUrl;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test successful coordinate fetching
    @Test
    public void testGetCoordinates_Success() {
        String pincode = "228714";
        LatLng latLng = new LatLng(1.3521, 103.8198);  // Mock LatLng response
        String url = locationServiceUrl+"/location/coordinates?pincode=" + pincode;

        // Set the location service URL
        // Mock the RestTemplate response
        when(restTemplate.getForObject(url, LatLng.class)).thenReturn(latLng);

        // Call the service method
        LatLng result = externalLocationService.getCoordinates(pincode);

        // Verify the result
        assertNotNull(result);
        assertEquals(latLng, result);
        verify(restTemplate, times(1)).getForObject(url, LatLng.class);
    }

    // Test failed coordinate fetching (Exception handling)
    @Test
    public void testGetCoordinates_Failure() {
        String pincode = "228714";
        String url = locationServiceUrl+"/location/coordinates?pincode=" + pincode;

        // Mock the RestTemplate to throw an exception
        when(restTemplate.getForObject(url, LatLng.class)).thenThrow(new RuntimeException("Service unavailable"));

        // Call the service method and expect a RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            externalLocationService.getCoordinates(pincode);
        });

        // Verify the exception message
        assertTrue(exception.getMessage().contains("Error fetching coordinates from external service"));
        verify(restTemplate, times(1)).getForObject(url, LatLng.class);
    }
}