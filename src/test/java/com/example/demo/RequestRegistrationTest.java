package com.example.demo;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class RequestRegistrationTest extends TestContainers{

    @Autowired
    private MockMvc mvc;

    @Test
    //@WithMockUser(username = "testUsername", roles = {"USER"})
    public void testRequestAuthorization() throws Exception {

        String username = "gwrghwhwehwehwehwehwh12";
        String password = "gwrghwhwehwehwehwehwh12";

        MvcResult result = mvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        Cookie[] cookies = result.getResponse().getCookies();

        mvc.perform(get("/search")
                        .session(session)
                        .param("searchQuery", "test")
                        .with(csrf())
                        .cookie(cookies))
                .andExpect(status().isOk());
    }

    @Test
    public void testRequestRegistration() throws Exception {
        String username = "RegistrationTest";
        String password = "RegistrationTest";

        mvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testRequestLogout() throws Exception {
        String username = "LogoutTest";
        String password = "LogoutTest";

        MvcResult result = mvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        mvc.perform(post("/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/login").session(session).with(csrf())).andExpect(status().isOk());
    }
}
