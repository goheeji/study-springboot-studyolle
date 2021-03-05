package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm") //signUpForm을 받을때 303검증(if)과 벨리데이터까지
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

@GetMapping("/sign-up")
    public String signUpForm(Model model){
    //model.addAttribute("signUpForm", new SignUpForm());
    model.addAttribute(new SignUpForm());
    return "account/sign-up";
}

@PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors){
    //@ModelAttribute 생략가능
    if(errors.hasErrors()){
        return "account/sign-up";
    }

    Account account = Account.builder()
            .email(signUpForm.getEmail())
            .nickname(signUpForm.getNickname())
            .password(signUpForm.getPassword()) //TODO encoding 해야함
            .studyCreatedByWeb(true)
            .studyEnrollResultByWeb(true)
            .studyUpdatedResultByWeb(true)
            .build();

    Account newAccount = accountRepository.save(account);

    newAccount.generateEmailCheckToken();
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(newAccount.getEmail());
    mailMessage.setSubject("스터디올래, 회원가입 인증"); //메일 제목
    mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
            "&email="+newAccount.getEmail());
    //TODO Email 보내기

    javaMailSender.send(mailMessage);


    //TODO 회원가입처리
    return "redirect:/";

}
}

