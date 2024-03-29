package com.web.billim.security.provider;
import com.web.billim.exception.UnAuthorizedException;
import com.web.billim.exception.handler.ErrorCode;
import com.web.billim.security.service.UserDetailServiceImpl;
import com.web.billim.security.domain.UserDetailsEntity;
import com.web.billim.security.dto.LoginAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UsernamPasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailServiceImpl userDetailService;
    private final PasswordEncoder passwordEncoder;

    public UsernamPasswordAuthenticationProvider(UserDetailServiceImpl userDetailService, PasswordEncoder passwordEncoder) {
        this.userDetailService = userDetailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        UserDetailsEntity user = userDetailService.loadUserByUsername(email);
        if(user != null && this.passwordEncoder.matches(password, user.getPassword()) && user.getUseYn().equals("Y") ){
            return new LoginAuthenticationToken(user.getAuthorities(),user.getMemberId());
        }else {
            throw new UnAuthorizedException(ErrorCode.INVALID_EMAIL_PASSWORD);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
