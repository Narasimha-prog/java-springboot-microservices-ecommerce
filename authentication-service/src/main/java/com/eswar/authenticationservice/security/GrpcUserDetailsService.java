package com.eswar.authenticationservice.security;

import com.eswar.authenticationservice.grpc.client.GrpcUserServiceClient;
import com.eswar.grpc.user.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GrpcUserDetailsService implements UserDetailsService {

    private final GrpcUserServiceClient grpcClient;

    public GrpcUserDetailsService(GrpcUserServiceClient grpcClient) {
        this.grpcClient = grpcClient;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserResponse user = grpcClient.getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        return new UserPrincipal(user);
    }
}
