package service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import model.Filme;
import model.Recomendacao;
import model.Usuario;
import util.IGeradorAleatorio;

public class RecomendadorService {
	private final ICatalogoFilmesAPI catalogo;
	private final IHistoricoUsuarioRepository historico;
	private final INotificadorPush notificador;
	private final IGeradorAleatorio gerador;
	private final CalculadoraScore calculadora;
	private final FiltroFilmes filtro;
	
	public RecomendadorService(ICatalogoFilmesAPI catalogo, IHistoricoUsuarioRepository historico,
			INotificadorPush notificador, IGeradorAleatorio gerador, CalculadoraScore calculadora,
			FiltroFilmes filtro) {
		this.catalogo = catalogo;
		this.historico = historico;
		this.notificador = notificador;
		this.gerador = gerador;
		this.calculadora = calculadora;
		this.filtro = filtro;
	}
	
	public List<Recomendacao> recomendar(Usuario usuario, int topN){
		List<Filme> todosFilmes;
		try {
			todosFilmes = catalogo.buscaTodosFilmes();
		} catch (Exception e) {
			return Collections.emptyList();
		}
		
		List<Filme> filmesFiltrados = filtro.filtrar(todosFilmes, usuario.getPerfil());
		
		if (filmesFiltrados.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Recomendacao> recomendacoes = filmesFiltrados.stream()
				.map(filme -> new Recomendacao(
						filme, 
						calculadora.calcular(filme, usuario.getPerfil()), 
						"Recomendação baseada no seu perfil"
				))
				.sorted(Comparator.comparingInt(Recomendacao::getScore).reversed()
						.thenComparing(r -> r.getFilme().getPopularidade(), Comparator.reverseOrder()))
				.limit(topN)
				.collect(Collectors.toList());
		
		historico.registrarRecomendacao(usuario, recomendacoes);
		
		if (usuario.isNotificacoesLigadas()) {
			notificador.enviarNotificacao("Você tem uma nova recomendação!");
		}
		
		return recomendacoes;
	}
	
	public Recomendacao recomendarAleatorio(Usuario usuario) {
		List<Filme> todosOsFilmes;
		
		try {
			todosOsFilmes = catalogo.buscaTodosFilmes();
		}catch (Exception e) {
			return null;
		}
		
		List<Filme> filtrados = filtro.filtrar(todosOsFilmes, usuario.getPerfil());
		if (filtrados.isEmpty()) {
			return null;
		}
		
		int indiceSorteado = gerador.sortearInteiro(0, filtrados.size() - 1);
		Filme filmeSorteado = filtrados.get(indiceSorteado);
		
		return new Recomendacao(filmeSorteado, 
				calculadora.calcular(filmeSorteado, usuario.getPerfil()), 
				"Selecionamos este filme diretamente para você!");
	}
}
