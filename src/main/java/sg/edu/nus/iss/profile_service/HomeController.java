package sg.edu.nus.iss.profile_service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// make this controller with a request mapping of / which shows "Welcome to Shopsmart Profile Management"
@RestController
public  class HomeController {
    @RequestMapping("/")
    public String home() {
        return "Welcome to Shopsmart Profile Management";
    }
}

