package model;

public class Usuario {
	private final String nome;
	private final int idade;
	private final PerfilCinefilo perfil;
	private boolean notificacoesLigadas;
	
	public Usuario(String nome, int idade, PerfilCinefilo perfil) {
		this.nome = nome;
		this.idade = idade;
		this.perfil = perfil;
	}

	public String getNome() {
		return nome;
	}

	public int getIdade() {
		return idade;
	}

	public PerfilCinefilo getPerfil() {
		return perfil;
	}
	
	public boolean isNotificacoesLigadas() {
		return notificacoesLigadas;
	}
	
	public void setNotificacoesLigadas(boolean notificacoesLigadas) {
		this.notificacoesLigadas = notificacoesLigadas;
	}
	
}
