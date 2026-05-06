package test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import exception.DuracaoInvalidaException;
import exception.NotaInvalidaException;
import exception.PesoInvalidoException;
import model.PerfilCinefilo;
import model.enums.Genero;

public class PerfilCinefiloTest {
	private PerfilCinefilo perfil;
	
	@BeforeEach
	void setUp() {
		perfil = new PerfilCinefilo(90, 150);
	}
	
	@Test
	@DisplayName("Deve permitir criar perfil com pesos válidos")
	void deveCriarPerfilQuandoPesosForemValidos() {
		perfil.setPeso(Genero.ACAO, 0.8);
		assertEquals(0.8, perfil.getPeso(Genero.ACAO));
	}
	
	@Test
	@DisplayName("Deve lançar exceção ao tentar colocar peso fora do intervalo 0.0 a 1.0")
	void deveLancarExcecaoQuandoPesoForInvalido() {
		assertThrows(PesoInvalidoException.class, () -> perfil.setPeso(Genero.ACAO, 1.5));
		assertThrows(PesoInvalidoException.class, () -> perfil.setPeso(Genero.DRAMA, -0.1));
	}
	
	@Test
	@DisplayName("Deve lançar exceção se duração mínima for maior que a máxima")
	void deveLancarExcecaoQuandoDuracaoMinimaForMaiorQueMaxima() {
		assertThrows(DuracaoInvalidaException.class, () -> new PerfilCinefilo(150, 90));
	}
	
	@Test
	@DisplayName("Deve lançar exceção ao adicionar nota fora do intervalo 1 a 5")
	void deveLancarExcecaoQuandoNotaEstiverForaDoIntervalo() {
		assertThrows(NotaInvalidaException.class, () -> perfil.adicionarNota("F01", 6));
		assertThrows(NotaInvalidaException.class, () -> perfil.adicionarNota("F02", 0));
	}
	
	@Test
	@DisplayName("Filme marcado como assistido deve aparecer no histórico")
	void deveAdicionarAoHistoricoQuandoMarcarFilmeAssistido() {
		perfil.adicionarNoHistorico("O Iluminado");
		assertTrue(perfil.getHistoricoFilmesAssistidos().contains("O Iluminado"));
	}
}
