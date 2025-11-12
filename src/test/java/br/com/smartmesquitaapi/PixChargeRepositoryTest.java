package br.com.smartmesquitaapi;

import br.com.smartmesquitaapi.repository.PixChargeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PixChargeRepositoryTest {

    @Autowired
    private PixChargeRepository repository;

    @Test
    void contextLoads() {
        assertNotNull(repository);
        System.out.println("✅ Repository carregado com sucesso!");
    }
}
