package com.pickleball_backend.pickleball.security;

import com.pickleball_backend.pickleball.entity.UserAccount;
import com.pickleball_backend.pickleball.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(usernameOrEmail)
                .or(() -> userAccountRepository.findByUser_Email(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Get userType from associated User entity
        String userType = account.getUser().getUserType();

        String authority = "ROLE_" + userType.toUpperCase();
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(authority)
        );

        return new org.springframework.security.core.userdetails.User(
                account.getUsername(),
                account.getPassword(),
                !account.getStatus().equals("DELETED"),  // enabled
                true,                                     // account non-expired
                true,                                     // credentials non-expired
                !account.isLocked(),                      // account non-locked
                authorities
        );
    }
}