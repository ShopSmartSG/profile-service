package sg.edu.nus.iss.profile_service.service;

import sg.edu.nus.iss.profile_service.model.Profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileService {
    Profile createProfile(Profile profile);
    void updateProfile(Profile profile);
    void deleteProfile(UUID id);
    Optional<Profile> getProfileById(String type, UUID id);

    List<Profile> getProfilesByType(String type);

    Optional<Profile> getProfileByEmailAddress(String email, String type);
}