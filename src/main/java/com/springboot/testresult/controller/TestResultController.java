package com.springboot.testresult.controller;

import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseDto;
import com.springboot.testresult.dto.TestResultDto;
import com.springboot.testresult.entity.TestResult;
import com.springboot.testresult.mapper.TestResultMapper;
import com.springboot.testresult.service.TestResultService;
import com.springboot.utils.CredentialUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/test")
@Validated
public class TestResultController {

    private final TestResultService testResultService;
    private final TestResultMapper testResultMapper;

    public TestResultController(TestResultService testResultService, TestResultMapper testResultMapper) {
        this.testResultService = testResultService;
        this.testResultMapper = testResultMapper;
    }

    @PostMapping
    public ResponseEntity postTestResult (@Valid @RequestBody TestResultDto.Post postDto,
                                          Authentication authentication) {

        postDto.setMemberId(Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId")));

        TestResult testResult = testResultMapper.testResultPostDtoToTestResult(postDto);

        testResultService.createTestResult(testResult, authentication);

        return new ResponseEntity<>(new SingleResponseDto<>(testResultMapper.testResultToTestResultResponseDto(testResult)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity getTestResults(@Positive @RequestParam int page,
                                         @Positive @RequestParam int size,
                                         Authentication authentication) {

        Page<TestResult> pageTestResult = testResultService.findTestResults(page - 1, size, authentication);

        List<TestResult> testResults = pageTestResult.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(testResultMapper.testResultsToTestResultResponseDtos(testResults), pageTestResult), HttpStatus.OK);
    }
}
