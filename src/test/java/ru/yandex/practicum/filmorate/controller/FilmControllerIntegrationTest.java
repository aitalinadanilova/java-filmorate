package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerIntegrationTest {

    private final MockMvc mockMvc;

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilms() throws Exception {
        mockMvc.perform(get("/films/common")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(3));
    }

    @Test
    @Sql(scripts = {"/common-films-test.sql"})
    void testGetCommonFilmsInvalidUser() throws Exception {
        mockMvc.perform(get("/films/common")
                        .param("userId", "999") // несуществующий пользователь
                        .param("friendId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCommonFilmsMissingParameters() throws Exception {
        mockMvc.perform(get("/films/common"))
                .andExpect(status().isBadRequest());
    }
}