package service;

import java.util.List;

import model.Recomendacao;
import model.Usuario;

public interface IHistoricoUsuarioRepository {
	void registrarRecomendacao(Usuario usuario, List<Recomendacao> recomendacoes);
}
