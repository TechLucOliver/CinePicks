package service;

import java.util.List;

import model.Recomendacao;
import model.Usuario;

public interface HistoricoUsuarioRepository {
	void registrarRecomendacao(Usuario usuario, List<Recomendacao> recomendacoes);
}
