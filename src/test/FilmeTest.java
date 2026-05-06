package test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Filme;
import model.enums.ClassificacaoEtaria;
import model.enums.Genero;
import model.enums.Idioma;

public class FilmeTest {
	@Test
	@DisplayName("Deve preencher todos os atributos corretamente quando o filme é criado")
	void devePreencherAtributosQuandoFilmeCriado() {
		Filme filme = new Filme(
				"F01", 
				"Duna: Parte Dois", 
				2024, 
				166, 
				List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA), 
				ClassificacaoEtaria.QUATORZE, 
				Idioma.EN, 
				92);
		
		assertAll(
				() -> assertEquals("F01", filme.getId()),
				() -> assertEquals("Duna: Parte Dois", filme.getTitulo()),
				() -> assertTrue(filme.getGeneros().contains(Genero.FICCAO_CIENTIFICA)),
				() -> assertTrue(filme.getGeneros().contains(Genero.DRAMA))
		);
	}
	
	@Test
	@DisplayName("Dois filmes com o mesmo ID devem ser considerados iguais")
	void deveConsiderarIguaisQuandoFilmesTemMesmoId() {
		Filme filme1 = new Filme(
				"F01", 
				"Duna", 
				2024, 
				166, 
				List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA), 
				ClassificacaoEtaria.QUATORZE, 
				Idioma.EN, 
				92);
		
		Filme filme2 = new Filme(
				"F01", 
				"Duna Diretor's Cut", 
				2025, 
				180, 
				List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA), 
				ClassificacaoEtaria.DEZESSEIS, 
				Idioma.EN, 
				95);
		
		assertEquals(filme1, filme2);
		assertEquals(filme1.hashCode(), filme2.hashCode());
	}
}
