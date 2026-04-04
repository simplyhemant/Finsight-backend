package simply.Finsight_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "System", description = "Root and Health Check endpoints")
public class Test {

    @Operation(summary = "Root Endpoint", description = "A simple welcome message to verify the API is up and running.")
    @GetMapping()
    public String Home(){
        return "Welcome to FinSight.";
    }
}