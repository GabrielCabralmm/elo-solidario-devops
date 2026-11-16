package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Categoria;

public record CategoriaResponse(Long id, String nome, Boolean perecivel) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getPerecivel());
    }
}
