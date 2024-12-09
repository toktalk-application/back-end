package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.*;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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
        List<Boolean> expectedResults = List.of(true, true, true, false, false, false, false, false, false, false);

        Counselor counselor = mock(Counselor.class);
        CounselorDto.Post postDto = mock(CounselorDto.Post.class);

        // when, then
        for(int i = 0; i< licenseCount.size(); i++){
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
            if(expectedResults.get(i)){
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
        // given
        License license1 = new License(1, null, "테스트용1", "테스트");
        License license2 = new License(2, null, "테스트용2", "테스트");
        License license3 = new License(3, null, "테스트용3", "테스트");
        License license4 = new License(4, null, "테스트용4", "테스트");
        License license5 = new License(5, null, "테스트용5", "테스트");

        // repository가 항상 반환할 상담사 객체 (라이센스를 1개 가지고 있음)
        Counselor testCounselor = new Counselor();
        testCounselor.setLicenses(new ArrayList<>(){{add(license1);}});
        // 반환값 고정
        lenient().when(counselorRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testCounselor));

        // 5개의 테스트 케이스 (키: 추가할 라이센스, 값: 정상 실행 여부)
        Map<List<License>, Boolean> testCases = Map.of(
                List.of(license1), true,
                List.of(license1, license2), true,
                List.of(license1, license2, license3), false,
                List.of(license1, license2, license3, license4), false,
                List.of(license1, license2, license3, license4, license5), false
        );

        // when, then
        for(Map.Entry<List<License>, Boolean> entry : testCases.entrySet()){
            if(entry.getValue()){
                assertDoesNotThrow(() -> counselorService.addLicense(1, entry.getKey()),
                        String.format("라이센스가 1개 있는 상태에서 %d개를 더 추가할 수 있어야 합니다.", entry.getKey().size())); ;
            }else{
                assertThrows(BusinessLogicException.class, () -> counselorService.addLicense(1, entry.getKey()),
                        String.format("라이센스가 1개 있는 상태에서 %d개를 추가하려 하면 예외가 발생해야 합니다.", entry.getKey().size()));
            }
        }
    }

    @Test
    void deleteLicense() {
        // given
        License license1 = new License(1, null, "테스트용1", "테스트");

        // 테스트 케이스
        int[] givenLicenseSize = new int[]{1,1,1,2,2,2,3,3,3}; // 상담사가 가지고 있는 라이센스 개수
        int[] licenseNumberToDelete = new int[]{0,1,2,1,2,3,1,2,3}; // 지울 라이센스의 번호 (인덱스 + 1)
        boolean[] expectedResults = new boolean[]{false, false, false, true, true, false, true, true, true}; // 예상 결과

        for(int i = 0; i< givenLicenseSize.length; i++){
            int finalI = i;

            // repository가 반환할 상담사 객체 (라이센스를 givenLicenseSize[i]개 가지고 있음)
            Counselor counselor = new Counselor();
            // 라이센스 동적으로 생성
            List<License> licenses = new ArrayList<>();
            for(int j = 0; j< givenLicenseSize[i]; j++) licenses.add(license1);
            counselor.setLicenses(licenses);

            // 반환값 고정
            lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

            // when, then
            if(expectedResults[i]){
                assertDoesNotThrow(() -> counselorService.deleteLicense(1, licenseNumberToDelete[finalI]),
                        String.format("라이센스가 %d개 있는 상황에서 %d번째 요소 삭제가 가능해야 합니다.", givenLicenseSize[finalI], licenseNumberToDelete[finalI]));
            }else{
                assertThrows(BusinessLogicException.class, () -> counselorService.deleteLicense(1, licenseNumberToDelete[finalI]),
                        String.format("라이센스가 %d개 있는 상황에서 %d번 요소를 삭제하려 하면 예외가 발생해야 합니다.", givenLicenseSize[finalI], licenseNumberToDelete[finalI]));
            }
        }
    }

    @Test
    void addCareer() {
        // given
        Career career1 = new Career(1, null, Career.Classification.CURRENT,"테스트","테스트용1");
        Career career2 = new Career(2, null, Career.Classification.CURRENT,"테스트","테스트용2");
        Career career3 = new Career(3, null, Career.Classification.CURRENT,"테스트","테스트용3");
        Career career4 = new Career(4, null, Career.Classification.CURRENT,"테스트","테스트용4");
        Career career5 = new Career(5, null, Career.Classification.CURRENT,"테스트","테스트용5");

        // 테스트 케이스
        Map<List<Career>, Boolean> given = Map.of(
                List.of(career1), true,
                List.of(career1, career2), true,
                List.of(career1, career2, career3), false,
                List.of(career1, career2, career3, career4), false,
                List.of(career1, career2, career3, career4, career5), false
        );

        // repository가 반환하게 할 상담사 객체
        Counselor counselor = new Counselor();
        // 경력사항을 1개 보유하고 있다 가정
        counselor.setCareers(new ArrayList<>(){{add(career1);}});
        // repository 반환값 고정
        lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

        // when, then
        for(Map.Entry<List<Career>, Boolean> entry : given.entrySet()){
            if(entry.getValue()){
                assertDoesNotThrow(() -> counselorService.addCareer(1, entry.getKey()),
                        String.format("경력사항이 1개 있을 때 %d개를 더 추가할 수 있어야 합니다", entry.getKey().size()));
            }else{
                assertThrows(BusinessLogicException.class, () -> counselorService.addCareer(1, entry.getKey()),
                        String.format("경력사항이 1개 있을 때 %d개를 더 추가하려 하면 예외가 발생해야 합니다", entry.getKey().size()));
            }
        }
    }

    @Test
    void deleteCareer() {
        // given
        Career career = new Career(1, null, Career.Classification.CURRENT,"테스트","테스트용");

        int[] givenCareerSizes = new int[]{1,1,1,2,2,2,3,3,3};
        int[] careerNumberToDelete = new int[]{0,1,2,1,2,3,1,2,3};
        boolean[] expectedResult = new boolean[]{false, false, false, true, true, false, true, true, true};

        // when, then
        for(int i = 0; i< givenCareerSizes.length; i++){
            int finalI = i;

            // repository가 반환할 상담사 객체
            Counselor counselor = new Counselor();
            counselor.setCareers(new ArrayList<>(){{
                for(int j = 0; j< givenCareerSizes[finalI]; j++) add(career);
            }});
            // 반환값 고정
            lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

            if(expectedResult[i]){
                assertDoesNotThrow(() -> counselorService.deleteCareer(1, careerNumberToDelete[finalI]),
                        String.format("경력사항이 %d개 있을 때, %d번째 요소를 삭제할 수 있어야 합니다.", givenCareerSizes[finalI], careerNumberToDelete[finalI]));
            }else{
                assertThrows(BusinessLogicException.class, () -> counselorService.deleteCareer(1, careerNumberToDelete[finalI]),
                        String.format("경력사항이 %d개 있을 때, %d번째 요소를 삭제하려 하면 예외가 발생해야 합니다.", givenCareerSizes[finalI], careerNumberToDelete[finalI]));
            }
        }
    }

    @Test
    void setDefaultDays() {
        // given
        // 기본 시간이 9~12시라고 가정
        Counselor counselor = new Counselor();
        counselor.setDefaultDays(Map.of(DayOfWeek.MONDAY,
                new DefaultDay(1, null, DayOfWeek.MONDAY, new ArrayList<>(){{
                    add(new DefaultTimeSlot(1, null, LocalTime.of(9,0), LocalTime.of(9,50)));
                    add(new DefaultTimeSlot(1, null, LocalTime.of(10,0), LocalTime.of(10,50)));
                    add(new DefaultTimeSlot(1, null, LocalTime.of(11,0), LocalTime.of(11,50)));
                }})));
        // repository 반환값 고정
        lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

        // 테스트 케이스 (키: 변경 시간, 값: 상담시간 갯수)
        List<CounselorDto.DefaultDays> testDtos = List.of(
                new CounselorDto.DefaultDays(DayOfWeek.MONDAY,
                        List.of(
                                LocalTime.of(9,0),
                                LocalTime.of(10,0)
                        )),
                new CounselorDto.DefaultDays(DayOfWeek.MONDAY,
                        List.of(
                                LocalTime.of(8,0),
                                LocalTime.of(9,0),
                                LocalTime.of(10,0)
                        )),
                new CounselorDto.DefaultDays(DayOfWeek.MONDAY,
                        List.of())
        );
        List<Integer> firstTimes = List.of(9, 8, -1);
        List<Integer> lastTimes = List.of(10, 10, -1);

        for(int i = 0; i< testDtos.size(); i++){
            // when
            counselorService.setDefaultDays(1, testDtos.get(i), false);
            List<DefaultTimeSlot> newTimes = counselor.getDefaultDays().get(DayOfWeek.MONDAY).getDefaultTimeSlots();

            // then
            int expectedSize = testDtos.get(i).getTimes().size();
            int actualSize = newTimes.size();
            assertEquals(expectedSize, actualSize, String.format("기본 상담 시간이 %d개의 슬롯으로 수정되어야 합니다.", expectedSize));

            if(expectedSize > 0){
                int firstTime = firstTimes.get(i);
                int actualFirstTime = newTimes.get(0).getStartTime().getHour();
                assertEquals(firstTime, actualFirstTime, String.format("첫 번째 시간은 %d시여야 합니다.", firstTime));

                int lastTime = lastTimes.get(i);
                int actualLastTime = newTimes.get(newTimes.size() - 1).getStartTime().getHour();
                assertEquals(lastTime, actualLastTime, String.format("마지막 시간은 %d시여야 합니다.", lastTime));
            }
        }
    }

    @Test
    void areDefaultDaysInitialized() {
        // given
        // repository의 반환값 고정
        Counselor counselor = new Counselor();
        counselor.setAvailableDates(new HashMap<>());
        lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

        // when, then
        boolean expected1 = false;
        boolean actual1 = counselorService.areDefaultDaysInitialized(1);
        assertEquals(expected1, actual1, "AvailableDates가 없는 경우 false를 반환해야 합니다.");

        // AvailableDate 넣어주면
        counselor.addAvailableDate(
                new AvailableDate(LocalDate.of(2024, 12, 6))
        );

        boolean expected2 = true;
        boolean actual2 = counselorService.areDefaultDaysInitialized(1);
        assertEquals(expected2, actual2, "AvailableDates가 생긴 후에는 true를 반환해야 합니다.");
    }

    @Test
    void addInitialAvailableTimes() {
        // given
        // repository가 반환할 상담사 객체
        Counselor counselor = new Counselor();
        lenient().when(counselorRepository.findById(anyLong())).thenReturn(Optional.of(counselor));

        // 모든 요일에 대해 기본 상담 시간 생성
        for(DayOfWeek day : DayOfWeek.values()){
            DefaultDay defaultDay = new DefaultDay();
            defaultDay.setDayOfWeek(day);
            defaultDay.setCounselor(counselor);

            CounselorDto.DefaultDays dto = new CounselorDto.DefaultDays(day, List.of(
                    LocalTime.of(9, 0)
            ));
            counselorService.setDefaultDays(1, dto, true);
        }
        // when
        counselorService.addInitialAvailableTimes(1, 1);
        // then
        assertThrows(BusinessLogicException.class, ()-> counselorService.getAvailableDate(1, LocalDate.now().minusDays(1)).getAvailableTimes().size(),
                "어제 날짜는 상담 가능 시간이 생성되지 않아야 합니다.");
        assertNotEquals(0, counselor.getAvailableDate(LocalDate.now()).getAvailableTimes().size(),
                "오늘 날짜부터 상담 가능 시간이 생성되어야 합니다.");
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