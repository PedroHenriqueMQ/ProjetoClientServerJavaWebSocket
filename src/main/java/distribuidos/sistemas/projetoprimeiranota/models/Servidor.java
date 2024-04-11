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
        Thread thread = new Thread(
            () -> {
                try {
                    switch (operation) {
                        case INICIAR_CONEXAO -> iniciarConexao();
                        case ENCERRAR_CONEXAO -> encerrarConexao();
                        case ENVIAR -> {
                            try {
                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                String nomeArquivo = dataInputStream.readUTF();

                                dataInputStream.close();

                                if (listarArquivosSalvos() != null) {
                                    for (File arquivo : listarArquivosSalvos()) {
                                        if (nomeArquivo.equals(arquivo.getName())) enviar(arquivo.getPath());
                                        break;
                                    }
                                }
                                else {
                                    System.out.println("Diretório de arquivos salvos inexistente ou vazio!");
                                    return;
                                }
                            } catch (NullPointerException error) {
                                System.out.println("Inputação de arquivo mal sucedida!");
                                System.err.println(error);
                            }
                        }
                        case RECEBER -> receber("src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/");
                        case EXCLUIR -> excluir();
                    }
                } catch (IOException error) {
                    System.out.println("Operação mal sucedida!");
                    System.err.println(error);
                    return;
                }
                System.out.println("Operação bem sucedida!");
            }
        );

        thread.start();
    }

    private synchronized void iniciarConexao() throws IOException {
        socket = serverSocket.accept();
        System.out.println("Inicialização de conexão bem sucedida!");
    }

    private synchronized void encerrarConexao() throws IOException {
        try {
            serverSocket.close();
            socket.close();
            System.out.println("Encerramento de conexão bem sucedido!");
        } catch (NullPointerException error) {
            System.out.println("Encerramento de conexão mal sucedido!");
            System.err.println(error);
        }
    }

    private synchronized void enviar(String caminhoArquivoEntrada) throws IOException {
        try {
            FileInputStream fileInputStream = new FileInputStream(caminhoArquivoEntrada);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Inputação de arquivo bem sucedida!");

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
            System.out.println("Inputação de arquivo mal sucedida!");
            System.err.println(error);
        }
    }

    private synchronized void receber(String caminhoArquivoSaida) throws IOException {
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
            System.out.println("Rebimento do arquivo bem sucedido!");
        } catch (FileNotFoundException | NullPointerException error) {
            System.out.println("Rebimento do arquivo mau sucedido!");
            System.err.println(error);
        }
    }

    private synchronized void excluir() throws IOException {
        InputStream inputStream;

        try {
            inputStream = socket.getInputStream();
        } catch (NullPointerException error) {
            System.out.println("Exclusão de arquivo mau sucedida!");
            System.err.println(error);
            return;
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder caminhoArquivo = new StringBuilder();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            caminhoArquivo.append(new String(buffer, 0, bytesRead));
        }

        File file = new File(caminhoArquivo.toString());

        if (listarArquivosSalvos() != null) {
            for (File arquivo : listarArquivosSalvos()) {
                if (file.equals(arquivo)) file.delete();
            }
        }
        else {
            System.out.println("Diretório de arquivos salvos inexistente ou vazio!");
            return;
        }
        System.out.println("Exclusão de arquivo bem sucedida!");

        inputStream.close();
    }

    private File[] listarArquivosSalvos() {
        String caminhoPadrao = "src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/";
        File diretorio = new File(caminhoPadrao);

        if(diretorio.exists() && diretorio.isDirectory()) {
            System.out.println("Listagem de arquivo bem sucedida!");
            return diretorio.listFiles();
        } else {
            System.out.println("Listagem de arquivo mau sucedida!");
            return null;
        }
    }
}
