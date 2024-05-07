package softeer.be33ma3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.dto.request.CenterSignUpDto;
import softeer.be33ma3.dto.request.LoginDto;
import softeer.be33ma3.dto.request.ClientSignUpDto;
import softeer.be33ma3.dto.response.LoginSuccessDto;
import softeer.be33ma3.jwt.JwtService;
import softeer.be33ma3.response.DataResponse;
import softeer.be33ma3.response.SingleResponse;
import softeer.be33ma3.service.MemberService;

import static softeer.be33ma3.jwt.JwtProperties.REFRESH_HEADER_STRING;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtService jwtService;

    @PostMapping(value = "/client/signUp", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> clientSignUp(@RequestPart(name = "request") @Valid ClientSignUpDto clientSignUpDto,
                                          @RequestPart(name = "profile", required = false) MultipartFile profile) {
        memberService.clientSignUp(clientSignUpDto, profile);

        return ResponseEntity.ok(SingleResponse.success("회원가입 성공"));
    }

    @PostMapping(value = "/center/signUp", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> centerSignUp(@RequestPart(name = "request") @Valid CenterSignUpDto centerSignUpDto,
                                          @RequestPart(name = "profile", required = false) MultipartFile profile) {
        memberService.centerSignUp(centerSignUpDto, profile);

        return ResponseEntity.ok(SingleResponse.success("회원가입 성공"));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto) {
        LoginSuccessDto loginSuccessDto = memberService.login(loginDto);

        return ResponseEntity.ok(DataResponse.success("로그인 성공", loginSuccessDto));
    }


    @PostMapping("/reissueToken")   //refreshToken 으로 accessToken 재발급
    public ResponseEntity<?> reissueToken(@RequestHeader(REFRESH_HEADER_STRING) String refreshToken) {
        String accessToken = jwtService.reissue(refreshToken);

        return ResponseEntity.ok(DataResponse.success("토큰 재발급 성공", accessToken));
    }
}
