/*
 * Copyright (c) 2020.
 * Fábio Nogueira de Lucena - Instituto de Informática (UFG)
 * Creative Commons Attribution 4.0 International License.
 */

package com.github.kyriosdata.rnds;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.commons.codec.binary.Base64;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A execução satisfatória dos testes depende de valores para as
 * seguintes variáveis de ambiente:
 *
 * <ul>
 *     <li><b>RNDS_AUTH</b>: endereço do serviço de autenticação.</li>
 *     <li><b>RNDS_EHR</b>: endereço do serviço de saúde.</li>
 *     <li><b>RNDS_CERTIFICADO_ARQUIVO</b>: caminho completo do arquivo
 *     contendo o certificado do laboratório.</li>
 *     <li><b>RNDS_CERTIFICADO_SENHA</b>: senha para acesso ao conteúdo do
 *     certificado digital.</li>
 * </ul>
 */
public class RNDSTest {

    private static char[] password;
    private static String keystore;
    private static String auth;
    private static String ehr;
    private static String token;

    private static final boolean DEBUG = false;

    @BeforeAll
    static void obtemConfiguracao() {
        if (!DEBUG) {
            Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
            Logger.getLogger("httpclient").setLevel(Level.OFF);
            Logger.getLogger("RNDS").setLevel(Level.OFF);
        } else {
            System.setProperty("javax.net.debug", "all");
        }

        // Serviço Auth
        auth = System.getenv("RNDS_AUTH");
        assertNotNull(auth, "Auth não definido");

        // Serviços EHR
        ehr = System.getenv("RNDS_EHR");
        assertNotNull(ehr, "EHR não definido");

        // Arquivo (certificado)
        keystore = System.getenv("RNDS_CERTIFICADO_ARQUIVO");
        assertTrue(Files.exists(Path.of(keystore)), "arquivo com " +
                "certificado inexistente");

        // Senha de acesso ao conteúdo do certificado (keystore)
        final String senha = System.getenv("RNDS_CERTIFICADO_SENHA");
        assertNotNull(senha, "senha de acesso ao certificado null");
        assertNotEquals("", senha.trim(), "senha vazia");
        password = senha.toCharArray();
    }

    @BeforeEach
    public void obtemToken() {
        token = RNDS.getToken(auth, keystore, password);
        assertNotNull(token);
    }

    @Test
    public void recuperarTokenViaVariaveisDeAmbiente() {
        String[] split_string = token.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];

        Base64 base64Url = new Base64(true);
        String header = new String(base64Url.decode(base64EncodedHeader));
        assertTrue(header.contains("kid"));
        assertTrue(header.contains("rnds auth"));
        assertTrue(header.contains("RS256"));

        String body = new String(base64Url.decode(base64EncodedBody));
        assertTrue(body.contains("RNDS-HMG"));
        assertTrue(body.contains("ICP-Brasil"));
    }

    @Test
    void cnesConhecido() {
        String cnes = RNDS.cnes(ehr, token, "2337991", "980016287385192");
        assertTrue(cnes.contains("LABORATORIO ROMULO ROCHA"));
    }

    @Test
    void cnesInvalidoNaoPodeSerEncontrado() {
        assertNull(RNDS.cnes(ehr, token, "233799", "980016287385192"));
    }

    @Test
    void profissionalPeloCns() {
        String cns = RNDS.profissional(ehr, token, "980016287385192",
                "980016287385192");
        assertTrue(cns.contains("SANTOS"));
    }

    @Test
    void profissionalPeloCpf() {
        String cns = RNDS.cpf(ehr, token, "01758263156",
                "980016287385192");
        System.out.println(cns);
        assertTrue(cns.contains("SANTOS"));
    }
}