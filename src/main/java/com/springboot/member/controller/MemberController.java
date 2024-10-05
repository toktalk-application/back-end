package com.springboot.member.controller;

import com.springboot.auth.userdetails.MemberDetailsService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {
    private final String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @PostMapping
    public ResponseEntity<Member> postMember(@RequestBody MemberDto.Post postDto){
        Member tempMember = memberMapper.memberPostDtoToMember(postDto);
        Member member = memberService.createMember(tempMember);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, member.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{memberId}")
    public SingleResponseEntity<MemberDto.Response> getMember(@PathVariable long memberId,
                                                              Authentication authentication){
        Member findMember = memberService.findMember(memberId);
        return new SingleResponseEntity<>(memberMapper.memberToMemberResponseDto(findMember), HttpStatus.OK);
    }

    // 사용자 검증후 Fcm 토큰을 받아 Fcm토큰을 저장하는 Api
    @PutMapping("/{memberId}/fcm-token")
    public ResponseEntity<?> updateFcmToken(@PathVariable long memberId,
                                            @RequestBody MemberDto.FcmTokenDto fcmTokenDto,
                                            Authentication authentication) {
        // 사용자의 로그인 id 를 추출
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();

        // userId을 이용해 memberId를 조회하는 메서드
        long authenticatedMemberId = memberService.getMemberIdByUserId(userId);

        // 위에서 찾은 memberId를 와 로그인한 memberId가 일치하는지 확인
        if (authenticatedMemberId != memberId) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access denied");
        }

        // 검증이 끝나면 최종적으로 해당 회원에 fcm토큰을 저장
        memberService.updateFcmToken(memberId, fcmTokenDto.getFcmToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
