package distribuidos.sistemas.projetoprimeiranota.models;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public enum Operation {
        INICIAR_CONEXAO,
        ENCERRAR_CONEXAO,
        ENVIAR,
        RECEBER,
        EXCLUIR
    }

    private final ServerSocket serverSocket = new ServerSocket(8080);
    private Socket socket = null;

    public Servidor() throws IOException {
    }

    public void realizarOperacao(Operation operation) {
        Thread thread = new Thread(() -> {
            try {
                if (operation.equals(Operation.ENCERRAR_CONEXAO)) {
                    synchronized (socket) {
                        encerrarConexao(socket);
                    }
                }
                else if (requisitarConexao()) {
                    synchronized (socket) {
                        switch (operation) {
                            case ENVIAR -> enviar(socket, "src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/");
                            case RECEBER -> receber(socket, "src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/");
                            case EXCLUIR -> excluir(socket);
                        }
                    }
                } else {
                    System.out.println("Servidor: Conexão rejeitada.");
                }
            } catch (IOException error) {
                System.out.println("Servidor: Operação mal sucedida!");
                System.err.println(error);
            }
        });

        thread.start();
    }

    private boolean requisitarConexao() throws IOException {
        socket = serverSocket.accept();
        System.out.println("Servidor: Aceite de conexão bem sucedido!");
        return true;
    }

    private void encerrarConexao(Socket socket) throws IOException {
        try {
            serverSocket.close();
            if(socket != null) socket.close();
            System.out.println("Servidor: Encerramento de conexão bem sucedido!");
        } catch (NullPointerException error) {
            System.out.println("Servidor: Encerramento de conexão mal sucedido!");
            System.err.println(error);
        }
    }

    private void enviar(Socket socket, String caminhoArquivoEntrada) throws IOException {
        try {
            FileInputStream fileInputStream = new FileInputStream(caminhoArquivoEntrada);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Servidor: Inputação de arquivo bem sucedida!");

            dataOutputStream.writeUTF(new File(caminhoArquivoEntrada).getName());

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }

            dataOutputStream.flush();
            dataOutputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException | NullPointerException error) {
            System.out.println("Servidor: Inputação de arquivo mal sucedida!");
            System.err.println(error);
        }
    }

    private void receber(Socket socket, String caminhoArquivoSaida) throws IOException {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String nomeArquivo = dataInputStream.readUTF();
            FileOutputStream fileOutputStream = new FileOutputStream(caminhoArquivoSaida + nomeArquivo);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();
            dataInputStream.close();
            System.out.println("Servidor: Rebimento do arquivo bem sucedido!");
        } catch (FileNotFoundException | NullPointerException error) {
            System.out.println("Servidor: Rebimento do arquivo mau sucedido!");
            System.err.println(error);
        }
    }

    private void excluir(Socket socket) throws IOException {
        InputStream inputStream;

        try {
            inputStream = socket.getInputStream();
        } catch (NullPointerException error) {
            System.out.println("Servidor: Exclusão de arquivo mau sucedida!");
            System.err.println(error);
            return;
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder nomeArquivo = new StringBuilder();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            nomeArquivo.append(new String(buffer, 0, bytesRead));
        }

        if (verificaArquivoSalvo(nomeArquivo.toString())) {
            File file = new File("src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/" + nomeArquivo);
            file.delete();
            System.out.println("Servidor: Exclusão de arquivo bem sucedida!");
        }

        inputStream.close();
    }

    private boolean verificaArquivoSalvo(String normeArquivo) {
        if (listarArquivosSalvos() != null) {
            for (File arquivo : listarArquivosSalvos()) if (arquivo.getName().equals(normeArquivo)) return true;
            System.out.println("Servidor: Arquivo não encontrado!");
            return false;
        }
        else {
            System.out.println("Servidor: Diretório de arquivos salvos inexistente ou vazio!");
            return false;
        }
    }

    private File[] listarArquivosSalvos() {
        String caminhoPadrao = "src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/";
        File diretorio = new File(caminhoPadrao);

        if(diretorio.exists() && diretorio.isDirectory()) return diretorio.listFiles();
        else return null;
    }
}
