package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.junit.jupiter.api.Tag;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import service.CatalogoFilmesAPI;
import service.HistoricoUsuarioRepository;
import service.NotificadorPush;
import service.RecomendadorService;
import util.GeradorAleatorio;

@ExtendWith(MockitoExtension.class)
public class RecomendadorServiceTest {
	@Mock private CatalogoFilmesAPI catalogo;
	@Mock private HistoricoUsuarioRepository historico;
	@Mock private NotificadorPush notificador;
	@Mock private GeradorAleatorio gerador;
	
	@Spy private CalculadoraScore calculadora;
	@Spy private FiltroFilmes filtro;
	@InjectMocks private RecomendadorService service;
	
	private Usuario usuario;
	private Filme filmeAcao;
	private Filme filmeComedia;
	
	@BeforeEach
	void setUp() {
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
	void naoDeve_Derrubar_Quando_CatalogoLancaExcecao() {
		when(catalogo.buscaTodosFilmes()).thenThrow(new RuntimeException("API offline"));
		
		List<Recomendacao> resultado = service.recomendar(usuario, 5);
		
		assertTrue(resultado.isEmpty());
		verify(notificador, never()).enviarNotificacao(anyString());
	}
	
	@Test
	@DisplayName("Deve ordenar recomendações por score decrescente")
	void deve_OrdenarPorScoreDesc_Quando_RecomendacaoTemMultiplosFilmes() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeComedia, filmeAcao));
		List<Recomendacao> resultado = service.recomendar(usuario, 5);
		
		assertEquals(2, resultado.size());
		
		assertTrue(resultado.get(0).getScore() >= resultado.get(1).getScore());
		assertEquals("Filme de ação", resultado.get(0).getFilme().getTitulo());
	}
	
	@Test
	@DisplayName("Deve registrar a recomendação no histórico chamando a dependência")
	void deve_RegistrarRecomendacao_UsandoArgumentCaptor() {
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
	void deve_ChamarNotificador_Quando_PushEstaHabilitado() {
		usuario.setNotificacoesLigadas(true);
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao));
		
		service.recomendar(usuario, 2);
		
		verify(notificador, times(1)).enviarNotificacao(anyString());
	}
	
	@Test
	@DisplayName("Deve usar Spy para verificar quantas vezes a CalculadoraScore foi chamada")
	void deve_VerificarChamadas_NaCalculadoraComSpy() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao, filmeComedia));
		
		service.recomendar(usuario, 5);
		
		verify(calculadora, times(2)).calcular(any(Filme.class), eq(usuario.getPerfil()));
	}
	
	@Test
	@Tag("integracao")
	@DisplayName("Cenário de Integração: Pipeline completo (Filtro + Calculadora + Ranking)")
	void deve_ExecutarPipelineCompletoDeRecomendacao() {
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
	
	@Test
	@DisplayName("Deve retornar os IDs na ordem correta do ranking")
	void deve_RetornarIds_NaOrdemCorreta() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeComedia, filmeAcao));
		List<Recomendacao> resultado = service.recomendar(usuario, 5);
		
		String[] idsReais = resultado.stream().map(r -> r.getFilme().getId()).toArray(String[]::new);
		String[] idsEsperados = {"F1", "F2"};
		
		assertArrayEquals(idsEsperados, idsReais, "A ordem do ranking de IDs está incorreta");
	}
	
	@Test
	@DisplayName("Falha no push não derruba o sistema e recomendações são distintas")
	void deve_NaoDerrubar_Quando_NotificadorFalha() {
		usuario.setNotificacoesLigadas(true);
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao, filmeComedia));
		
		doThrow(new RuntimeException("Servidor offline"))
				.when(notificador).enviarNotificacao(anyString());
		
		List<Recomendacao> resultado = assertDoesNotThrow(() -> service.recomendar(usuario, 5));
		
		assertNotEquals(resultado.get(0).getFilme(), resultado.get(1).getFilme());
	}
	
	@Test
	@DisplayName("Deve devolver no máximo o top N pedido")
	void deve_RespeitarOTamanhoPedidoNoTopN() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao, filmeAcao, filmeAcao));

		List<Recomendacao> resultado = service.recomendar(usuario, 2);

		assertEquals(2, resultado.size(), "O tamanho da lista deve respeitar o parâmetro topN");
	}

	@Test
	@DisplayName("Em caso de empate de score, deve desempatar por popularidade")
	void deve_DesempatarPorPopularidade_Quando_ScoreForIgual() {
		Filme acaoMenosPopular = new Filme("F10", "Ação Lado B", 2010, 115, List.of(Genero.ACAO), ClassificacaoEtaria.DOZE, Idioma.PT, 50);
		Filme acaoMaisPopular = new Filme("F11", "Blockbuster", 2020, 115, List.of(Genero.ACAO), ClassificacaoEtaria.DOZE, Idioma.PT, 99);
		
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(acaoMenosPopular, acaoMaisPopular));

		List<Recomendacao> resultado = service.recomendar(usuario, 5);

		assertEquals(2, resultado.size());
		assertEquals("Blockbuster", resultado.get(0).getFilme().getTitulo());
	}

	@Test
	@DisplayName("Deve retornar recomendação vazia se o catálogo estiver vazio")
	void deve_RetornarVazio_Quando_CatalogoVazio() {
		when(catalogo.buscaTodosFilmes()).thenReturn(Collections.emptyList());

		List<Recomendacao> resultado = service.recomendar(usuario, 5);

		assertTrue(resultado.isEmpty());
	}

	@Test
	@DisplayName("Modo Surpreenda-me deve usar stub sequencial para múltiplos sorteios")
	void deve_DevolverFilmeAleatorio_NoSurpreendaMe() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao, filmeComedia));
		
		when(gerador.sortearInteiro(0, 1)).thenReturn(1).thenReturn(0);

		Recomendacao primeiraChamada = service.recomendarAleatorio(usuario);
		Recomendacao segundaChamada = service.recomendarAleatorio(usuario);

		assertNotNull(primeiraChamada);
		assertEquals("Filme de comédia", primeiraChamada.getFilme().getTitulo(), "A 1ª chamada usou o índice 1 (Comédia)");
		
		assertNotNull(segundaChamada);
		assertEquals("Filme de ação", segundaChamada.getFilme().getTitulo(), "A 2ª chamada usou o índice 0 (Ação)");
		
		verify(gerador, times(2)).sortearInteiro(0, 1);
	}
	
	@Test
	@DisplayName("Deve verificar interações usando Matchers, anyList e atLeastOnce")
	void deve_VerificarInteracoes_ComMatchers() {
		when(catalogo.buscaTodosFilmes()).thenReturn(List.of(filmeAcao));

		service.recomendar(usuario, 5);

		verify(catalogo, atLeastOnce()).buscaTodosFilmes();

		verify(historico).registrarRecomendacao(eq(usuario), anyList());

		verify(notificador, never()).enviarNotificacao(anyString());
	}
	
	
	
}
