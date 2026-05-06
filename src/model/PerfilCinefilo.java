package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.DuracaoInvalidaException;
import exception.PesoInvalidoException;
import model.enums.ClassificacaoEtaria;
import model.enums.Genero;
import model.enums.Idioma;

public class PerfilCinefilo {
	private final Map<Genero, Double> pesosGeneros = new HashMap<>();
	private final int duracaoMaxima;
	private final int duracaoMinima;
	private ClassificacaoEtaria classificacaoMaxima;
	private List<Idioma> idiomasAceitos = new ArrayList<>();
	private List<String> historicoFilmesAssistidos = new ArrayList<>();
	private Map<String, Integer> notasFilmes = new HashMap<>();
	
	public PerfilCinefilo(int duracaoMaxima, int duracaoMinima) throws DuracaoInvalidaException {
		if(duracaoMinima > duracaoMaxima) {
			throw new DuracaoInvalidaException("A duração mínima não pode ser maior do que a duração máxima!");
		}
		this.duracaoMaxima = duracaoMaxima;
		this.duracaoMinima = duracaoMinima;
	}
	
	public void setPeso(Genero genero, double peso) throws PesoInvalidoException {
		if (peso < 0.0 || peso > 1.0) {
			throw new PesoInvalidoException("O peso deve estar entre 0.0 e 1.0");
		}
		pesosGeneros.put(genero, peso);
	}
	
	public double getPeso(Genero genero) {
		return pesosGeneros.getOrDefault(genero, 0.5);
	}
	
	public ClassificacaoEtaria getClassificacaoMaxima() {
		return classificacaoMaxima;
	}

	public void setClassificacaoMaxima(ClassificacaoEtaria classificacaoMaxima) {
		this.classificacaoMaxima = classificacaoMaxima;
	}

	public List<Idioma> getIdiomasAceitos() {
		return idiomasAceitos;
	}

	public void adicionarIdiomaAceito(Idioma idioma) {
		this.idiomasAceitos.add(idioma);
	}

	public List<String> getHistoricoFilmesAssistidos() {
		return historicoFilmesAssistidos;
	}

	public void adicionarNoHistorico(String tituloFilme) {
		this.historicoFilmesAssistidos.add(tituloFilme);
	}

	public Map<String, Integer> getNotasFilmes() {
		return notasFilmes;
	}

	public void setNotasFilmes(Map<String, Integer> notasFilmes) {
		this.notasFilmes = notasFilmes;
	}

	public Map<Genero, Double> getPesosGeneros() {
		return pesosGeneros;
	}

	public int getDuracaoMaxima() {
		return duracaoMaxima;
	}

	public int getDuracaoMinima() {
		return duracaoMinima;
	}
	
	
}
