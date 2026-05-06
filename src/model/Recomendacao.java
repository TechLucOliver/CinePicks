package model;

public class Recomendacao {
	private final Filme filme;
	private final int score;
	private final String justificativa;
	
	public Recomendacao(Filme filme, int score, String justificativa) {
		this.filme = filme;
		this.score = score;
		this.justificativa = justificativa;
	}
	
	public Filme getFilme() {
		return filme;
	}
	
	public String getJustificativa() {
		return justificativa;
	}
	
	public int getScore() {
		return score;
	}
}
