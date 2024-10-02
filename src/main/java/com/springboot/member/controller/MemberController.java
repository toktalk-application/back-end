package com.springboot.member.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.response.SingleResponseDto;
import com.springboot.response.SingleResponseEntity;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {
    private final String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @PostMapping
    public ResponseEntity<?> postMember(@RequestBody MemberDto.Post postDto){
        Member tempMember = memberMapper.memberPostDtoToMember(postDto);
        Member member = memberService.createMember(tempMember);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, member.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<?> getMember(@PathVariable long memberId,
                                                              Authentication authentication){
        Member findMember = memberService.findMember(memberId);
        /*return new SingleResponseEntity<>(memberMapper.memberToMemberResponseDto(findMember), HttpStatus.OK);*/
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(findMember)), HttpStatus.OK
        );
    }

    @PatchMapping
    public ResponseEntity<?> patchMember(Authentication authentication,
                                         @RequestBody MemberDto.Patch patchDto){
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        LoginDto.UserType userType = auth.getUserType();
        // Member만 이용 가능한 요청
        if(!userType.equals(LoginDto.UserType.MEMBER)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        Map<String, String> credentials = (Map<String, String>)(authentication.getCredentials());

        Member member = memberMapper.memberPatchDtoToMember(patchDto);
        long memberId = Long.parseLong(credentials.get("memberId"));
        member.setMemberId(memberId);

        // 서비스 로직 실행
        Member patchedMember = memberService.updateMember(member);

        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(patchedMember)), HttpStatus.OK
        );
    }
}
