package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studyolle.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();

    @BeforeEach
    void beforeEach(){
        zoneRepository.save(testZone);
    }
    @AfterEach
    void afterEach(){

        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("heeji")
    @DisplayName("????????? ?????? ?????? ?????? ???")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("heeji")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account heeji = accountRepository.findByNickname("heeji");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(heeji.getZones().contains(zone));
    }

    @WithAccount("heeji")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void removeZone() throws Exception {
        Account heeji = accountRepository.findByNickname("heeji");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(heeji, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(heeji.getZones().contains(zone));
    }

    @WithAccount("heeji")
    @DisplayName("????????? ?????? ?????? ???")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));

    }

    @WithAccount("heeji")
    @DisplayName("????????? ?????? ??????")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(tagForm))
        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account heeji = accountRepository.findByNickname("heeji");
        assertTrue(heeji.getTags().contains(newTag));

    }
    @WithAccount("heeji")
    @DisplayName("????????? ?????? ??????")
    @Test
    void removeTag() throws Exception {
        Account heeji = accountRepository.findByNickname("heeji");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(heeji, newTag);

        assertTrue(heeji.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(heeji.getTags().contains(newTag));
    }




    @WithAccount("heeji")
    @DisplayName("????????? ?????? ???")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("heeji")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile() throws Exception{
        String bio = "?????? ????????? ???????????? ??????.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account heeji = accountRepository.findByNickname("heeji");
        assertEquals(bio, heeji.getBio());
    }

    @WithAccount("heeji")
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile_error() throws Exception{
        String bio = "?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.?????? ????????? ???????????? ??????.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account heeji = accountRepository.findByNickname("heeji");
        assertNull(heeji.getBio());
    }

    @WithAccount("heeji")
    @DisplayName("???????????? ?????? ???")
    @Test
    void updatePasswordForm() throws Exception{
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("heeji")
    @DisplayName("???????????? ???????????? - ????????? ??????")
    @Test
    void updatePassword_success() throws Exception{
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));
        Account heeji = accountRepository.findByNickname("heeji");
        assertTrue(passwordEncoder.matches("12345678", heeji.getPassword()));
    }

    @WithAccount("heeji")
    @DisplayName("???????????? ?????? - ????????? ?????? - ???????????? ?????????")
    @Test
    void updatePassword_error() throws Exception{
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());
    }
}