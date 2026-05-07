package test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.junit.jupiter.api.Tag;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.tools.javac.jvm.Gen;

import model.Filme;
import model.PerfilCinefilo;
import model.Recomendacao;
import model.Usuario;
import model.enums.ClassificacaoEtaria;
import model.enums.Genero;
import model.enums.Idioma;
import service.CalculadoraScore;
import service.FiltroFilmes;
import service.ICatalogoFilmesAPI;
import service.IHistoricoUsuarioRepository;
import service.INotificadorPush;
import service.RecomendadorService;
import util.IGeradorAleatorio;

@ExtendWith(MockitoExtension.class)
public class RecomendadorServiceTest {
	@Mock private ICatalogoFilmesAPI catalogo;
	@Mock private IHistoricoUsuarioRepository historico;
	@Mock private INotificadorPush notificador;
	@Mock private IGeradorAleatorio gerador;
	
	@Spy private CalculadoraScore calculadora;
	private FiltroFilmes filtro;
	private RecomendadorService service;
	
	private Usuario usuario;
	private Filme filmeAcao;
	private Filme filmeComedia;
	
	@BeforeEach
	void setUp() {
		filtro = new FiltroFilmes();
		
		service = new RecomendadorService(catalogo, historico, notificador, gerador, calculadora, filtro);
		
		PerfilCinefilo perfil = new PerfilCinefilo(150, 90);
		perfil.setClassificacaoMaxima(ClassificacaoEtaria.DEZOITO);
		perfil.adicionarIdiomaAceito(Idioma.PT);
		perfil.setPeso(Genero.ACAO, 1.0);
		perfil.setPeso(Genero.COMEDIA, 0.5);
		
		usuario = new Usuario("Lucas", 26, perfil);
		usuario.setNotificacoesLigadas(false);
		
		filmeAcao = new Filme("F1", "Filme de ação", 2007, 115, List.of(Genero.ACAO), ClassificacaoEtaria.DEZOITO, Idioma.PT, 90);
		filmeComedia = new Filme("F2", "Filme de comédia", 2010, 100, List.of(Genero.COMEDIA), ClassificacaoEtaria.DOZE, Idioma.PT, 70);
	}
	
	
	@Test
	@DisplayName("Deve retornar lista vazia e não quebrar quando a API do catálogo lançar exceção")
	void naoDeveDerrubarQuandoCatalogoLancaExcecao() {
		when(catalogo.buscaTodosFilmes()).thenThrow(new RuntimeException("API offline"));
		
		List<Recomendacao> resultado = service.recomendar(usuario, 5);
		
		assertTrue(resultado.isEmpty());
		verify(notificador, never()).enviarNotificacao(anyString());
	}
	
	@Test
	@DisplayName("Deve ordenar recomendações por score decrescente")
	void deveOrdenarPorScoreDescQuandoRecomendacaoTemMultiplosFilmes() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeComedia, filmeAcao));
		List<Recomendacao> resultado = service.recomendar(usuario, 5);
		
		assertEquals(2, resultado.size());
		
		assertTrue(resultado.get(0).getScore() >= resultado.get(1).getScore());
		assertEquals("Filme de ação", resultado.get(0).getFilme().getTitulo());
	}
	
	@Test
	@DisplayName("Deve registrar a recomendação no histórico chamando a dependência")
	void deveRegistrarRecomendacaoUsandoArgumentCaptor() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao));
		
		ArgumentCaptor<List<Recomendacao>> captor = ArgumentCaptor.forClass(List.class);
		
		service.recomendar(usuario, 3);
		
		verify(historico).registrarRecomendacao(eq(usuario), captor.capture());
		
		List<Recomendacao> recomendacoesSalvas = captor.getValue();
		assertEquals(1, recomendacoesSalvas.size());
		assertEquals("Filme de ação", recomendacoesSalvas.get(0).getFilme().getTitulo());
	}
	
	@Test
	@DisplayName("Deve chamar o notificador push apenas se estiver habilitado no perfil")
	void deveChamarNotificadorQuandoPushEstaHabilitado() {
		usuario.setNotificacoesLigadas(true);
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao));
		
		service.recomendar(usuario, 2);
		
		verify(notificador, times(1)).enviarNotificacao(anyString());
	}
	
	@Test
	@DisplayName("Deve usar Spy para verificar quantas vezes a CalculadoraScore foi chamada")
	void deveVerificarChamadasNaCalculadoraComSpy() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao, filmeComedia));
		
		service.recomendar(usuario, 5);
		
		verify(calculadora, times(2)).calcular(any(Filme.class), eq(usuario.getPerfil()));
	}
	
	@Test
	@Tag("integracao")
	@DisplayName("Cenário de Integração: Pipeline completo (Filtro + Calculadora + Ranking)")
	void deveExecutarPipelineCompletoDeRecomendacao() {
		PerfilCinefilo perfilInt = new PerfilCinefilo(180, 90);
		perfilInt.setClassificacaoMaxima(ClassificacaoEtaria.DEZESSEIS);
		perfilInt.adicionarIdiomaAceito(Idioma.EN);
		perfilInt.setPeso(Genero.FICCAO_CIENTIFICA, 0.9);
		perfilInt.setPeso(Genero.DRAMA, 0.6);
		perfilInt.setPeso(Genero.TERROR, 0.0);
		
		Usuario maria = new Usuario("Maria", 28, perfilInt);
		
		Filme duna = new Filme("F01", "Duna", 2024, 166, List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA), ClassificacaoEtaria.QUATORZE, Idioma.EN, 92);
		Filme oIluminado = new Filme("F03", "O Iluminado", 1980, 146, List.of(Genero.TERROR), ClassificacaoEtaria.DEZOITO, Idioma.EN, 88);
		Filme aChegada = new Filme("F07", "A Chegada", 2016, 116, List.of(Genero.FICCAO_CIENTIFICA, Genero.DRAMA), ClassificacaoEtaria.DOZE, Idioma.EN, 84);
		
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(duna, oIluminado, aChegada));
		
		List<Recomendacao> resultado = service.recomendar(maria, 5);
		
		assertEquals(2, resultado.size(), "Deve filtrar o filme de terror e devolver apenas 2 filmes");
		
		assertEquals("Duna", resultado.get(0).getFilme().getTitulo(), "Duna deve ser o primeiro do ranking");
		assertEquals("A Chegada", resultado.get(1).getFilme().getTitulo(), "A Chegada deve vir em segundo lugar");
	}
	
	
	
	
	
	
	
}
