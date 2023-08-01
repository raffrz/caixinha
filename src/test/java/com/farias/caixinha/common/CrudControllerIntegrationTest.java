package com.farias.caixinha.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farias.caixinha.common.CrudControllerIntegrationTest.CrudControllerTestConfiguration.FooController;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@WebMvcTest(controllers = FooController.class)
public class CrudControllerIntegrationTest {

    @TestConfiguration
    static class CrudControllerTestConfiguration {

        @Bean
        Converter<Foo, FooDTO> fooToFooDTOConverter() {
            return new FooToFooDTOConverter();
        }

        @Bean
        Converter<FooDTO, Foo> fooDTOToFooConverter() {
            return new FooDTOToFooConverter();
        }

        @RestController
        @RequestMapping("/foo")
        public class FooController extends CrudController<Foo, FooDTO> {

            @Override
            public Class<Foo> getEntityClass() {
                return Foo.class;
            }

            @Override
            public Class<FooDTO> getDTOClass() {
                return FooDTO.class;
            }

        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrudService<Foo> fooService;

    @SpyBean
    private ModelMapper modelMapper;

    private Foo foo;

    @BeforeEach
    public void setup() {
        foo = new Foo(UUID.randomUUID(), "Foo bar");
        when(fooService.create(any(Foo.class))).thenReturn(foo);
    }

    @Nested
    class CreateEntityTest {
        @Test
        public void testShouldCreateEntity() throws Exception {
            FooDTO fooDTO = new FooDTO();
            fooDTO.setFoo("Foo bar");
            String requestBody = objectMapper.writeValueAsString(fooDTO);

            mockMvc.perform(
                    MockMvcRequestBuilders.post("/foo").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.foo").value("Foo bar"));
        }
    }

    @Nested
    class FindByIdTest {
        @Test
        public void testShouldFindOneEntity() throws Exception {
            var id = UUID.randomUUID();
            var foo = new Foo(id, "Foo bar");
            when(fooService.findById(id)).thenReturn(Optional.of(foo));

            mockMvc.perform(MockMvcRequestBuilders.get("/foo/" + id))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.foo").value("Foo bar"));
        }

        @Test
        public void testShouldReturnNotFoundWhenEntityNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(fooService.findById(id)).thenReturn(Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.get("/foo/" + id))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    class ListAllTest {
        @Test
        public void testShouldListAllEntities() throws Exception {
            Foo foo1 = new Foo(UUID.randomUUID(), "Foo bar");
            Foo foo2 = new Foo(UUID.randomUUID(), "Bar bar");
            when(fooService.listAll()).thenReturn(Arrays.asList(foo1, foo2));

            mockMvc.perform(MockMvcRequestBuilders.get("/foo"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].foo").value("Foo bar"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").isNotEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].foo").value("Bar bar"));
        }
    }

    @Nested
    class UpdateTest {
        @Test
        public void testShouldUpdate() throws Exception {
            var id = UUID.randomUUID();
            var foo = new Foo(id, "Foo bar");
            var fooRequest = new FooDTO();
            fooRequest.setId(id);
            fooRequest.setFoo("Foo bar bar");
            var requestBody = objectMapper.writeValueAsString(fooRequest);
            when(fooService.findById(id)).thenReturn(Optional.of(foo));
            when(fooService.update(any())).thenReturn(new Foo(id, "Foo bar bar"));

            mockMvc.perform(
                    MockMvcRequestBuilders.put("/foo/" + id).contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.foo").value("Foo bar bar"));
        }

        @Test
        public void testShouldReturnNotFoundWhenEntityNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(fooService.findById(id)).thenReturn(Optional.empty());
            var requestBody = objectMapper.writeValueAsString(new FooDTO(id, "Foo bar"));

            mockMvc.perform(MockMvcRequestBuilders.put("/foo/" + id).contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    class PartialUpdateTest {
        @Test
        public void testShouldUpdatePartial() throws Exception {
            var id = UUID.randomUUID();
            var foo = new Foo(id, "Foo", "Bar bar");
            var fooRequest = new FooDTO();
            fooRequest.setId(id);
            fooRequest.setBar("Bar bar");
            var requestBody = objectMapper.writeValueAsString(fooRequest);
            when(fooService.findById(id)).thenReturn(Optional.of(new Foo(id, "Foo", "Bar")));
            when(fooService.update(foo)).thenReturn(foo);

            mockMvc.perform(
                    MockMvcRequestBuilders.patch("/foo/" + id).contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id.toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.foo").value("Foo"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.bar").value("Bar bar"));
        }

        @Test
        public void testShouldReturnNotFoundWhenEntityNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(fooService.findById(id)).thenReturn(Optional.empty());
            var requestBody = objectMapper.writeValueAsString(new FooDTO(id, "Foo bar"));

            mockMvc.perform(MockMvcRequestBuilders.patch("/foo/" + id).contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    class DeleteTest {
        @Test
        public void testShouldDelete() throws Exception {
            var id = UUID.randomUUID();
            when(fooService.existsById(id)).thenReturn(true);

            mockMvc.perform(MockMvcRequestBuilders.delete("/foo/" + id))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());
        }

        @Test
        public void testShouldReturnNotFoundWhenEntityNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(fooService.existsById(id)).thenReturn(false);

            mockMvc.perform(MockMvcRequestBuilders.delete("/foo/" + id))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    static class FooToFooDTOConverter implements Converter<Foo, FooDTO> {
        ModelMapper mapper = new ModelMapper();

        @Override
        public FooDTO convert(Foo source) {
            return mapper.map(source, FooDTO.class);
        }
    };

    static class FooDTOToFooConverter implements Converter<FooDTO, Foo> {
        ModelMapper mapper = new ModelMapper();

        @Override
        public Foo convert(FooDTO source) {
            return mapper.map(source, Foo.class);
        }
    };

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class FooDTO {
        UUID id;
        String foo;
        String bar;

        FooDTO(UUID id, String foo) {
            this.id = id;
            this.foo = foo;
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Foo implements BaseEntity {
        UUID id;
        String foo;
        String bar;

        Foo(UUID id, String foo) {
            this.id = id;
            this.foo = foo;
        }
    }
}
