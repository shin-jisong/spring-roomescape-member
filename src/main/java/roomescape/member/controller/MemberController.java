package roomescape.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.member.dto.LoginRequest;
import roomescape.member.service.MemberService;

@Controller
public class MemberController {
    private static final String TOKEN = "token";
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    //TODO 회원가입

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest loginRequest,
                                      HttpServletResponse httpServletResponse) {
        String token = memberService.checkLogin(loginRequest);
        httpServletResponse.addCookie(new Cookie(TOKEN, token));
        return ResponseEntity.ok().build();
    }

}
