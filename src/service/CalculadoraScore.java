package service;

import model.Filme;
import model.PerfilCinefilo;
import model.enums.Genero;

public class CalculadoraScore {
	static final double PESO_GENERO = 0.50;
	static final double PESO_DURACAO = 0.20;
	static final double PESO_POPULARIDADE = 0.15;
	static final double PESO_AFINIDADE = 0.15;
	
	public int calcular(Filme filme, PerfilCinefilo perfil) {
		double scoreGenero = calcularScoreGenero(filme, perfil);
		double scoreDuracao = calcularScoreDuracao(filme, perfil);
		double scorePopularidade = filme.getPopularidade();
		double scoreAfinidade = calcularScoreAfinidade(filme, perfil);
		
		double notaFinal = (scoreGenero * PESO_GENERO) + 
							(scoreDuracao * PESO_DURACAO) + 
							(scorePopularidade * PESO_POPULARIDADE) + 
							(scoreAfinidade * PESO_AFINIDADE);
		
		return (int) Math.min(100, Math.max(0, Math.round(notaFinal)));
	}
	
	private double calcularScoreGenero(Filme filme, PerfilCinefilo perfil) {
		if (filme.getGeneros().isEmpty()) {
			return 0;
		}
		
		double somaPesos = 0;
		
		for (Genero g : filme.getGeneros()) {
			somaPesos += perfil.getPeso(g);
		}
		
		return (somaPesos / filme.getGeneros().size()) * 100;
	}
	
	private double calcularScoreDuracao(Filme filme, PerfilCinefilo perfil) {
		
		int duracao = filme.getDuracao();
		int minimo = perfil.getDuracaoMinima();
		int maximo = perfil.getDuracaoMaxima();
		
		if(duracao >= minimo && duracao <= maximo) {
			return 100.0;
		}
		
		int diferenca = duracao < minimo ? minimo - duracao : duracao - maximo;
		double penalidade = diferenca * 2.0;
		return Math.max(0.0, 100.0 - penalidade);
		
	}
	
	private double calcularScoreAfinidade(Filme filme, PerfilCinefilo perfil) {
		boolean gostaDosFilmes = perfil.getNotasFilmes().values().stream().anyMatch(nota -> nota >= 4);
		
		return gostaDosFilmes ? 100.0 : 50.0;
	}
}
