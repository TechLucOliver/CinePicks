package test;

import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Filme;
import model.PerfilCinefilo;
import model.enums.ClassificacaoEtaria;
import model.enums.Genero;
import model.enums.Idioma;
import service.FiltroFilmes;

@Tag("unitario")
public class FiltroFilmesTest {
	private FiltroFilmes filtro;
	private PerfilCinefilo perfil;
	private Filme filme;
	
	@BeforeEach
	void setUp() {
		filtro = new FiltroFilmes();
		perfil = new PerfilCinefilo(150, 90);
		perfil.setClassificacaoMaxima(ClassificacaoEtaria.DEZESSEIS);
		perfil.adicionarIdiomaAceito(Idioma.EN);
		
		filme = new Filme(
				"F02", 
				"Ela (Her)", 
				2013, 
				126, 
				List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA, Genero.ROMANCE), 
				ClassificacaoEtaria.DEZESSEIS, 
				Idioma.EN, 
				78
		);
	}
	
	@Nested
	@DisplayName("Cenários: Quando o Catálogo está Vazio")
	class QuandoCatalogoEstaVazio {
		
		@Test
		@DisplayName("Deve retornar lista vazia (nunca null) quando o catálogo for vazio")
		void deve_RetornarListaVazia_Quando_CatalogoEstaVazio() {
			List<Filme> resultado = filtro.filtrar(new ArrayList<>(), perfil);
			
			assertNotNull(resultado);
			assertTrue(resultado.isEmpty());
		}
	}
	
	
	
	@Test
	@DisplayName("Deve remover filme do catálogo se já foi assistido")
	void deve_RemoverFilme_Quando_JaFoiAssistido() {
		perfil.adicionarNoHistorico("Ela (Her)");
		
		List<Filme> resultado = filtro.filtrar(List.of(filme), perfil);
		
		assertTrue(resultado.isEmpty());
	}
	
	@Test
	@DisplayName("Deve remover filme se a classificação for maior que a permitida")
	void deve_RemoverFilme_Quando_AcimaDaClassificacaoDoUsuario() {
		Filme filmeAcimaDaIdadeDoUsuario = new Filme(
				"F05", 
				"Tropa de Elite", 
				2007, 
				115, 
				List.of(Genero.ACAO, Genero.DRAMA), 
				ClassificacaoEtaria.DEZOITO, 
				Idioma.PT, 
				80
		);
				
		List<Filme> resultado = filtro.filtrar(List.of(filmeAcimaDaIdadeDoUsuario), perfil);
		
		assertTrue(resultado.isEmpty());
	}
	
	@Test
	@DisplayName("Deve remover filme se estiver em idioma não preferido pelo usuario")
	void deve_RemoverFilme_Quando_IdiomaAceito() {
		Filme filmeFrances = new Filme(
				"F08", 
				"Intocáveis", 
				2011, 
				112, 
				List.of(Genero.COMEDIA, Genero.DRAMA), 
				ClassificacaoEtaria.QUATORZE, 
				Idioma.FR, 
				65
		);
		
		List<Filme> resultado = filtro.filtrar(List.of(filmeFrances), perfil);
		
		assertTrue(resultado.isEmpty());
	}
	
	@Test
	@DisplayName("Deve remover filme se a pessoa explicitamente não gosta do gênero (peso 0.0)")
	void deve_RemoverFilme_Quando_GeneroTemPesoZero() {
		perfil.setPeso(Genero.ROMANCE, 0.0);
		
		List<Filme> resultado = filtro.filtrar(List.of(filme), perfil);
		
		assertTrue(resultado.isEmpty());
	}
	
	@Test
	@DisplayName("A lista filtrada não deve conter filme já assistido")
	void naoDeve_ConterFilmeAssistido() {
		perfil.adicionarNoHistorico(filme.getTitulo());
		List<Filme> resultado = filtro.filtrar(List.of(filme), perfil);
		
		assertFalse(resultado.contains(filme), "O filme assistido deve ser removido");
	}
	
	
}
