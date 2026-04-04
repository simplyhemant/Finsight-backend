package simply.Finsight_backend.service;

import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.repository.UserRepository;
import simply.Finsight_backend.exception.ResourceNotFoundException;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new CustomUserDetails(user);
    }
}