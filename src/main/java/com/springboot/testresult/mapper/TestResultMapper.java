package com.springboot.testresult.mapper;

import com.springboot.testresult.dto.TestResultDto;
import com.springboot.testresult.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestResultMapper {
    @Mapping(source = "memberId", target = "member.memberId")
    TestResult testResultPostDtoToTestResult(TestResultDto.Post post);
    @Mapping(source = "testLevel.description", target = "description")
    TestResultDto.Response testResultToTestResultResponseDto(TestResult testResult);
    List<TestResultDto.Response> testResultsToTestResultResponseDtos(List<TestResult> testResults);
}
