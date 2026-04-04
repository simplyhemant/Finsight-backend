package simply.Finsight_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import simply.Finsight_backend.service.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Automatically injects CustomUserDetailsService
public class JwtTokenValidator extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization"); // Using standard header name

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (JwtTokenProvider.isTokenValid(token)) {
                String email = JwtTokenProvider.getEmailFromToken(token);

                // BEST APPROACH: Load the full UserDetails object
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Set userDetails as the Principal, NOT the email string
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}