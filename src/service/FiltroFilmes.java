package service;

import java.util.List;
import java.util.stream.Collectors;

import model.Filme;
import model.PerfilCinefilo;

public class FiltroFilmes {
	public List<Filme> filtrar(List<Filme> catalogo, PerfilCinefilo perfil){
		if (catalogo == null || catalogo.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		
		return catalogo.stream()
				.filter(filme -> naoAssistido(filme, perfil))
				.filter(filme -> idadePermitida(filme, perfil))
				.filter(filme -> idiomaAceito(filme, perfil))
				.filter(filme -> generoAceito(filme, perfil))
				.collect(Collectors.toList());
	}
	
	private boolean naoAssistido(Filme filme, PerfilCinefilo perfil) {
		return !perfil.getHistoricoFilmesAssistidos().contains(filme.getTitulo());
	}
	
	private boolean idadePermitida(Filme filme, PerfilCinefilo perfil) {
		return filme.getClassificacao().getIdade() <= perfil.getClassificacaoMaxima().getIdade();
	}
	
	private boolean idiomaAceito(Filme filme, PerfilCinefilo perfil) {
		return perfil.getIdiomasAceitos().contains(filme.getIdioma());
	}
	
	private boolean generoAceito(Filme filme, PerfilCinefilo perfil) {
		return filme.getGeneros()
				.stream()
				.noneMatch(genero -> perfil.getPeso(genero) == 0.0);
	}
}
