package sg.edu.nus.iss.profile_service.model;

import lombok.Data;

@Data
public class LatLng {
    private Double lat;
    private Double lng;

    public LatLng(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    // Getters and setters
}