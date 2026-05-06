package model;

public class Usuario {
	private final String nome;
	private final int idade;
	private final PerfilCinefilo perfil;
	
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
	
	
}
