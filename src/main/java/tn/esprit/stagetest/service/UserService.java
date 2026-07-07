package tn.esprit.stagetest.service;

import tn.esprit.stagetest.dto.RegistrationDto;
import tn.esprit.stagetest.entity.User;

public interface UserService {

    User registerUser(RegistrationDto registrationDto);

    boolean usernameExists(String username);

    boolean emailExists(String email);
}
