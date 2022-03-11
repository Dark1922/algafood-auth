package com.algaworks.algafoodauth.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.algaworks.algafoodauth.domain.Usuario;
import com.algaworks.algafoodauth.domain.UsuarioRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class JpaUserDetailService implements UserDetailsService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Transactional
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//dados um nome do usuário agente tme que devolver os dados do usuário do banco
		Usuario usuario = usuarioRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com e-mail informado"));

		return new AuthUser(usuario, getAuthorities(usuario));
	}

	private Collection<GrantedAuthority> getAuthorities(Usuario usuario) {
              //stream de todas permissao
      return usuario.getGrupos().stream().flatMap(grupo -> grupo.getPermissoes().stream())
			  .map(permissao -> new SimpleGrantedAuthority(permissao.getNome().toUpperCase()))
			  .collect(Collectors.toSet()); //set n permite entradas duplicadas de permissão
	}

}
