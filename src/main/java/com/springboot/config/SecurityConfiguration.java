package com.springboot.config;

import com.springboot.auth.CustomAuthenticationProvider;
import com.springboot.auth.filter.JwtAuthenticationFilter;
import com.springboot.auth.filter.JwtVerificationFilter;
import com.springboot.auth.handler.MemberAccessDeniedHandler;
import com.springboot.auth.handler.MemberAuthenticationEntryPoint;
import com.springboot.auth.handler.MemberAuthenticationFailureHandler;
import com.springboot.auth.handler.MemberAuthenticationSuccessHandler;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.service.CounselorService;
import com.springboot.member.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberService memberService;
    private final CounselorService counselorService;
    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils, CustomAuthenticationProvider customAuthenticationProvider,
                                 RedisTemplate<String, Object> redisTemplate, @Lazy MemberService memberService, @Lazy CounselorService counselorService) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.redisTemplate = redisTemplate;
        this.memberService = memberService;
        this.counselorService = counselorService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors(Customizer.withDefaults())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .apply(new CustomFilterConfigurer()) //
                .and()
                .authenticationProvider(customAuthenticationProvider) // 커스텀 authenticationProvider 적용
                // 인증 실패했을 때 처리(아래 3줄이 한 세트)
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint())
                .accessDeniedHandler(new MemberAccessDeniedHandler())
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        // 여기에 경로 지정 안 된 요청들은 accessDeniedHandler가 적용 안 됨
                        // 그래서 토큰 안 넣으면 500 에러가 뜸. 처리되지 않은 예외이기 때문에
                        .antMatchers("/ws/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/members").permitAll()
                        .antMatchers(HttpMethod.POST, "/members/**").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/members/userid-availabilities").permitAll()
                        .antMatchers(HttpMethod.GET, "/members/nickname-availabilities").permitAll()
                        .antMatchers(HttpMethod.GET, "/members").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/members/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/members").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/members").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.POST, "/counselors").permitAll()
                        .antMatchers(HttpMethod.POST, "/counselors/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/counselors").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/counselors/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/counselors").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/counselors").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.POST, "/reservations").hasRole("USER")
                        .antMatchers(HttpMethod.GET, "/reservations").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/reservations/**").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/reservation").hasRole("USER")
                        .anyRequest().permitAll()
                );
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PATCH","DELETE","OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder){
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            JwtAuthenticationFilter jwtAuthenticationFilter =
                    new JwtAuthenticationFilter(authenticationManager,jwtTokenizer);
            jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler(memberService, counselorService));
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, redisTemplate);
            builder.addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }
}