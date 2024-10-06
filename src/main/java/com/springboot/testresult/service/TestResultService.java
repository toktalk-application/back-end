package com.springboot.testresult.service;

import com.springboot.auth.dto.LoginDto;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.testresult.entity.TestResult;
import com.springboot.testresult.repository.TestResultRepository;
import com.springboot.utils.CredentialUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TestResultService {
    private final TestResultRepository testResultRepository;
    private final MemberService memberService;

    public TestResultService(TestResultRepository testResultRepository, MemberService memberService) {
        this.testResultRepository = testResultRepository;
        this.memberService = memberService;
    }

    public TestResult createTestResult(TestResult testResult, Authentication authentication) {

        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.MEMBER)) {
            throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);
        }
        Member findMember = memberService.findMember(testResult.getMember().getMemberId());

        testResult.setMember(findMember);

        setTestLevelAndComment(testResult);

        return testResult;
    }

    public Page<TestResult> findTestResults(int page, int size, Authentication authentication) {

        Member findMember = memberService.findMember(Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId")));

        if(findMember.getTestResults().isEmpty()){
            return Page.empty();
        }

        TestResult testResult = verifiedExistTestResult(findMember.getTestResults().get(findMember.getTestResults().size()-1).getTestResultId());

        if (testResult.getMember().getMemberId() != Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"))) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_DENIED);
        }

        Pageable pageable = PageRequest.of(page, size);

        return testResultRepository.findByMember(pageable, findMember);
    }
    private void setTestLevelAndComment(TestResult testResult) {
        int score = testResult.getScore();

        if (score >= 0 && score <= 4) {
            testResult.setTestLevel(TestResult.TestLevel.NORMAL);
            testResult.setComment("적응상의 지장을 초래할만한 우울 관련 증상을 거의 보고하지 않았습니다.");
        } else if (score >= 5 && score <= 9) {
            testResult.setTestLevel(TestResult.TestLevel.MILD);
            testResult.setComment("경미한 수준의 우울감이 있으나 일상생활에 지장을 줄 정도는 아닙니다.");
        } else if (score >= 10 && score <= 14) {
            testResult.setTestLevel(TestResult.TestLevel.MODERATE);
            testResult.setComment("중간수준의 우울감을 비교적 자주 경험하는 것으로 보고하였습니다. 직업적, 사회적 적응에 일부 영향을 미칠 수 있어 주의 깊은 관찰과 관심이 필요합니다.");
        } else if (score >= 15 && score <= 19) {
            testResult.setTestLevel(TestResult.TestLevel.MODERATELY_SEVERE);
            testResult.setComment("약간 심한 수준의 우울감을 자주 경험하는 것으로 보고하였습니다. 직업적, 사회적 적응에 일부 영향을 미칠 경우, 정신건강 전문가의 도움을 받아보시기를 권해 드립니다.");
        } else if (score >= 20 && score <= 27) {
            testResult.setTestLevel(TestResult.TestLevel.SEVERE);
            testResult.setComment("광범위한 우울 증상을 매우 자주, 심한 수준에서 경험하는 것으로 보고하였습니다. 일상생활의 다양한 영역에서 어려움이 초래될 경우, 추가적인 평가나 정신건강 전문가의 도움을 받아보시기를 권해 드립니다.");
        } else {
            throw new IllegalArgumentException("Invalid score range: " + score);
        }
    }
    private TestResult verifiedExistTestResult(long testResultId) {
        Optional<TestResult> optionalTestResult = testResultRepository.findById(testResultId);

        return optionalTestResult.orElseThrow(() -> new BusinessLogicException(ExceptionCode.TEST_RESULT_NOT_FOUND));
    }
}
