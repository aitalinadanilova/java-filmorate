package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorDbStorage.class})
class DirectorDbStorageTest {

    private final DirectorDbStorage directorStorage;

    @Test
    void testCreateAndFindDirector() {
        Director director = Director.builder().name("James Cameron").build();
        Director created = directorStorage.create(director);

        Director saved = directorStorage.findById(created.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("James Cameron");
    }

    @Test
    void testUpdateDirector() {
        Director created = directorStorage.create(Director.builder().name("Old Name").build());
        created.setName("New Name");
        directorStorage.update(created);

        Director updated = directorStorage.findById(created.getId());
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void testFindAllDirectors() {
        directorStorage.create(Director.builder().name("Director 1").build());
        directorStorage.create(Director.builder().name("Director 2").build());

        List<Director> all = directorStorage.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void testDeleteDirector() {
        Director created = directorStorage.create(Director.builder().name("To Delete").build());
        directorStorage.deleteById(created.getId());

        List<Director> all = directorStorage.findAll();
        assertThat(all).isEmpty();
    }
}