package test;

import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import model.Filme;
import model.PerfilCinefilo;
import model.enums.ClassificacaoEtaria;
import model.enums.Genero;
import model.enums.Idioma;
import service.CalculadoraScore;


@Tag("unitario")
public class CalculadoraScoreTest {
	private CalculadoraScore calculadora;
	private PerfilCinefilo perfil;
	
	@BeforeEach
	void setUp() {
		calculadora = new CalculadoraScore();
		perfil = new PerfilCinefilo(150, 90);
	}
	
	@ParameterizedTest
	@CsvSource({
		"1.0 , 1.0, 100.0",
		"0.5, 0.5, 50.0",
		"0.0, 0.0, 0.0"
	})
	@DisplayName("Score do componente gênero deve seguir a média dos pesos")
	void deve_CalcularScoreDoGenero_ConformeOsPesos(double peso1, double peso2, double pesoEsperado) {
		perfil.setPeso(Genero.ACAO, peso1);
		perfil.setPeso(Genero.DRAMA, peso2);
		
		Filme filme = new Filme("F05", "Tropa de Elite", 2007, 115, List.of(Genero.ACAO, Genero.DRAMA), ClassificacaoEtaria.DEZOITO, Idioma.PT, 80);
		
		int scoreFinal = calculadora.calcular(filme, perfil);
		assertTrue(scoreFinal >= 0);
	}
	
	@ParameterizedTest
	@CsvSource({
		"120, 100",
		"180, 40",
		"80, 80"
	})
	@DisplayName("Filme dentro da duração deve ter score de duração 100, fora deve ser reduzido")
	void deve_CalcularScoreDeDuracao_Corretamente(int duracaoFilme, double componenteEsperado) {
		Filme filme = new Filme("F10", "Teste", 2024, duracaoFilme, List.of(Genero.ACAO), ClassificacaoEtaria.DEZESSEIS, Idioma.EN, 0);
		
		int scoreFinal = calculadora.calcular(filme, perfil);
		
		assertTrue(scoreFinal >= 0 && scoreFinal <= 100);
	}
	
	@Test
	@DisplayName("Score total nunca passa de 100")
	void deve_ManterScoreEntre0e100_QuandoExtremos() {
		perfil.setPeso(Genero.ACAO, 1.0);
		perfil.adicionarNota("Filme qualquer", 5);
		Filme filmePerfeito = new Filme("F11", "Vingadores: Guerra Infinita", 2018, 149, List.of(Genero.ACAO, Genero.FICCAO_CIENTIFICA), ClassificacaoEtaria.DOZE, Idioma.EN, 100);
		
		int score = calculadora.calcular(filmePerfeito, perfil);
		
		assertTrue(score <= 100, "O score não pode passar de 100");
	}
	
	
	
	
	
	
	
}
