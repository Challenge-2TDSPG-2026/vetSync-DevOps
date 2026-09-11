package br.com.fiap.VetSync.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TokenBlacklist {

    private final Set<String> tokensRevogados = ConcurrentHashMap.newKeySet();

    public void revogar(String token) {
        tokensRevogados.add(token);
    }

    public boolean isRevogado(String token) {
        return tokensRevogados.contains(token);
    }
}