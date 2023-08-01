package com.farias.caixinha.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootTest
@ActiveProfiles("test")
public class CrudServiceIntegrationTest {

    @Autowired
    FooService fooService;

    @Autowired
    FooRepository fooRepository;

    @BeforeEach
    void tearDown() {
        fooRepository.deleteAll();
    }

    @Nested
    class CreateTest {
        @Test
        public void testCreateFoo() {
            var foo = new Foo();
            foo.setBar("Foo bar");

            var result = fooService.create(foo);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getBar()).isEqualTo("Foo bar");

            var persistedFoo = fooRepository.findById(result.getId());
            assertThat(persistedFoo).isPresent().get().isEqualTo(result);
        }
    }

    @Nested
    class FindAllTest {

        @BeforeEach
        void tearUp() {
            var foo1 = new Foo();
            foo1.setBar("Foo bar");
            var foo2 = new Foo();
            foo2.setBar("Foo bar bar");
            fooRepository.saveAll(Arrays.asList(foo1, foo2));
        }

        @Test
        public void testShouldListAll() {
            var result = fooService.listAll();

            assertThat(result).isNotEmpty().hasSize(2);
            assertThat(result.get(0).getBar()).isEqualTo("Foo bar");
            assertThat(result.get(1).getBar()).isEqualTo("Foo bar bar");
        }
    }

    @Nested
    class FindByIdTest {
        UUID fooId;

        @BeforeEach
        void tearUp() {
            Foo foo = new Foo();
            foo.setBar("Foo bar");
            var persistedFoo = fooRepository.save(foo);
            fooId = persistedFoo.getId();
        }

        @Test
        public void testShouldFindById() {
            var result = fooService.findById(fooId);

            assertThat(result).isPresent();
            assertThat(result.get().getBar()).isEqualTo("Foo bar");
        }

        @Test
        public void testShouldReturnEmptyOptionalWhenEntityNotFound() {
            var result = fooService.findById(UUID.randomUUID());

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class UpdateTest {
        UUID fooId;

        @BeforeEach
        void setUp() {
            Foo foo = new Foo();
            foo.setBar("Foo bar");
            var persistedFoo = fooRepository.save(foo);
            fooId = persistedFoo.getId();
        }

        @Test
        public void testShouldUpdateEntity() {
            Foo updatedFoo = new Foo();
            updatedFoo.setId(fooId);
            updatedFoo.setBar("Updated foo bar");

            fooService.update(updatedFoo);

            var result = fooService.findById(fooId);
            assertThat(result).isPresent();
            assertThat(result.get().getBar()).isEqualTo("Updated foo bar");
        }

        @Test
        public void testShouldThrowExceptionWhenEntityNotFound() {
            Foo updatedFoo = new Foo();
            updatedFoo.setId(UUID.randomUUID());
            updatedFoo.setBar("Updated foo bar");

            assertThrows(EntityNotFoundException.class, () -> {
                fooService.update(updatedFoo);
            });
        }
    }

    @Nested
    class DeleteByIdTest {
        UUID fooId;

        @BeforeEach
        void setUp() {
            Foo foo = new Foo();
            foo.setBar("Foo bar");
            var persistedFoo = fooRepository.save(foo);
            fooId = persistedFoo.getId();
        }

        @Test
        public void testShouldDeleteEntity() {
            fooService.deleteById(fooId);

            var result = fooService.existsById(fooId);

            assertThat(result).isFalse();
        }

        @Test
        public void testShouldThrowExceptionWhenEntityNotFound() {
            UUID id = UUID.randomUUID();
            assertThrows(EntityNotFoundException.class, () -> {
                fooService.deleteById(id);
            });
        }
    }

    @Nested
    class ExistsByIdTest {
        UUID fooId;

        @BeforeEach
        void setUp() {
            Foo foo = new Foo();
            foo.setBar("Foo bar");
            var persistedFoo = fooRepository.save(foo);
            fooId = persistedFoo.getId();
        }

        @Test
        public void testShouldReturnTrueWhenEntityExists() {
            boolean exists = fooService.existsById(fooId);

            assertThat(exists).isTrue();
        }

        @Test
        public void testShouldReturnFalseWhenEntityDoesNotExist() {
            boolean exists = fooService.existsById(UUID.randomUUID());

            assertThat(exists).isFalse();
        }
    }

}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class Foo implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    String bar;
}

@Repository
@Profile("test")
interface FooRepository extends JpaRepository<Foo, UUID> {

}

@Service
@Profile("test")
class FooService extends CrudService<Foo> {

}
