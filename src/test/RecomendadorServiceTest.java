package test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	
	private CalculadoraScore calculadora;
	private FiltroFilmes filtro;
	private RecomendadorService service;
	
	private Usuario usuario;
	private Filme filmeAcao;
	private Filme filmeComedia;
	
	@BeforeEach
	void setUp() {
		calculadora = new CalculadoraScore();
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
	
	
	
	
	
	
	
	
	
}
