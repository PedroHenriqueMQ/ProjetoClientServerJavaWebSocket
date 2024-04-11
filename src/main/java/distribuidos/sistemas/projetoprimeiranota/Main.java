package distribuidos.sistemas.projetoprimeiranota;

import distribuidos.sistemas.projetoprimeiranota.models.Cliente;
import distribuidos.sistemas.projetoprimeiranota.models.Servidor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Servidor servidor = new Servidor();
        Cliente cliente = new Cliente("localhost", 8080);

        enviarArquivoClienteParaServidor(servidor, cliente);
        fazerDownloadArquivoDoServidor(servidor, cliente);
        excluirArquivoServidor(servidor, cliente);
    }

    static void enviarArquivoClienteParaServidor(Servidor servidor, Cliente cliente) throws IOException {
        //Inicia conexão
        servidor.realizarOperacao(Servidor.Operation.INICIAR_CONEXAO);
        cliente.realizarConexao();

        //Realiza Operação
        cliente.enviar("src/main/resources/distribuidos/sistemas/projetoprimeiranota/imagem.jpg");
        servidor.realizarOperacao(Servidor.Operation.RECEBER);

        //Encerra conexão
        servidor.realizarOperacao(Servidor.Operation.ENCERRAR_CONEXAO);
        cliente.encerrarConexao();
    }

    static void fazerDownloadArquivoDoServidor(Servidor servidor, Cliente cliente) throws IOException {
        //Inicia conexão
        servidor.realizarOperacao(Servidor.Operation.INICIAR_CONEXAO);
        cliente.realizarConexao();

        //Realiza operação
        cliente.requerirDownload("imagem.jpg");
        servidor.realizarOperacao(Servidor.Operation.ENVIAR);
        cliente.receberDownload("src/main/resources/distribuidos/sistemas/projetoprimeiranota/imagem.jpg");

        //Encerra conexão
        servidor.realizarOperacao(Servidor.Operation.ENCERRAR_CONEXAO);
        cliente.encerrarConexao();
    }

    static void excluirArquivoServidor(Servidor servidor, Cliente cliente) throws IOException {
        //Inicia conexão
        servidor.realizarOperacao(Servidor.Operation.INICIAR_CONEXAO);
        cliente.realizarConexao();

        //Realiza Operação
        cliente.excluir("imagem.jpg");
        servidor.realizarOperacao(Servidor.Operation.EXCLUIR);

        //Encerra conexão
        servidor.realizarOperacao(Servidor.Operation.ENCERRAR_CONEXAO);
        cliente.encerrarConexao();
    }
}
