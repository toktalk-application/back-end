package com.springboot.member.controller;

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
}
