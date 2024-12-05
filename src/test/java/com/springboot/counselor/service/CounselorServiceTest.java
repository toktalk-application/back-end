package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.Career;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.entity.License;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounselorServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CounselorRepository counselorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomAuthorityUtils customAuthorityUtils;

    @InjectMocks
    private CounselorService counselorService;


    @Test
    void createCounselor() {
        // given
        List<Integer> licenseCount = List.of(1,2,3,4,5,4,3,2,1,2);
        List<Integer> careersCount = List.of(1,2,3,1,2,3,4,5,6,7);
        List<String> usernames = List.of("alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliett");
        List<Boolean> isMustBePassed = List.of(true, true, true, false, false, false, false, false, false, false);

        Counselor counselor = mock(Counselor.class);
        CounselorDto.Post postDto = mock(CounselorDto.Post.class);

        // when, then
        for(int i = 0; i< 10; i++){
            // 아이디는 위에 명시된 값으로 고정
            lenient().when(counselor.getUserId()).thenReturn(usernames.get(i));
            // 라이선스의 갯수는 위에 명시된 값으로 고정
            List<License> licenses = new ArrayList<>();
            for(int j = 0; j< licenseCount.get(i); j++){
                licenses.add(new License(1, null, "테스트용 면허", "테스트"));
            }
            lenient().when(postDto.getLicenses()).thenReturn(licenses);
            // 경력사항의 갯수는 위에 명시된 값으로 고정
            List<Career> careers = new ArrayList<>();
            for(int j = 0; j< careersCount.get(i); j++){
                careers.add(new Career(1, null, Career.Classification.CURRENT, "테스트", "테스트용 경력"));
            }
            lenient().when(postDto.getCareers()).thenReturn(careers);

            // 각각의 케이스에서 비즈니스 로직 예외가 발생하는지 확인하기
            if(isMustBePassed.get(i)){
                assertDoesNotThrow(() -> counselorService.createCounselor(counselor, postDto),
                        String.format("자격증이 %d개, 경력사항이 %d개이고 아이디가 %s일 경우 정상적으로 계정이 생성되어야 합니다.",
                                licenseCount.get(i), careersCount.get(i), usernames.get(i)));
            }else{
                assertThrows(BusinessLogicException.class, () -> counselorService.createCounselor(counselor, postDto),
                        String.format("자격증이 %d개, 경력사항이 %d개이고 아이디가 %s일 경우 예외가 발생해야 합니다.",
                                licenseCount.get(i), careersCount.get(i), usernames.get(i)));
            }
        }
    }

    @Test
    void addLicense() {
    }

    @Test
    void deleteLicense() {
    }

    @Test
    void addCareer() {
    }

    @Test
    void addKeyword() {
    }

    @Test
    void deleteCareer() {
    }

    @Test
    void setDefaultDays() {
    }

    @Test
    void areDefaultDaysInitialized() {
    }

    @Test
    void addInitialAvailableTimes() {
    }

    @Test
    void addExtraAvailableTimes() {
    }

    @Test
    void getAvailableDate() {
    }

    @Test
    void getFilteredAvailableDate() {
    }

    @Test
    void updateAvailableDate() {
    }

    @Test
    void getDefaultTimesOfDay() {
    }

    @Test
    void findCounselor() {
    }

    @Test
    void testFindCounselor() {
    }

    @Test
    void updateCounselor() {
    }

    @Test
    void getAllActiveCounselors() {
    }

    @Test
    void updateFcmToken() {
    }

    @Test
    void getCounselorIdByUserId() {
    }

    @Test
    void getReservationCount() {
    }

    @Test
    void quitCounselor() {
    }
}