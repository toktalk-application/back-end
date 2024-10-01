package com.springboot.auth.userdetails;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class CounselorDetailsService implements UserDetailsService {
    private final CounselorRepository counselorRepository;
    private final CustomAuthorityUtils authorityUtils;

    public CounselorDetailsService(CounselorRepository counselorRepository, CustomAuthorityUtils authorityUtils) {
        this.counselorRepository = counselorRepository;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(username);
        Counselor findCounselor = optionalCounselor.orElseThrow(() -> new BusinessLogicException(ExceptionCode.COUNSELOR_NOT_FOUND));
        return new CounselorDetails(findCounselor);
    }

    private final class CounselorDetails extends Counselor implements UserDetails{
        CounselorDetails(Counselor counselor){
            setCounselorId(counselor.getCounselorId());
            setUserId(counselor.getUserId());
            setPassword(counselor.getPassword());
            setRoles(counselor.getRoles());
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorityUtils.createAuthorities(this.getRoles());
        }

        @Override
        public String getUsername() {
            return getUserId();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
