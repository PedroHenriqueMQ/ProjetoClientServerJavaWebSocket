package distribuidos.sistemas.projetoprimeiranota.models;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public enum Operation {
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
                if (requisitarConexao()) {
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

    private void enviar(Socket socket, String caminhoArquivoLocal) throws IOException {
        String nomeArquivo = receberNomeArquivo(socket);
        if (!verificarArquivoSalvo(nomeArquivo)) return;

        try {
            socket = serverSocket.accept();
            FileInputStream fileInputStream = new FileInputStream(caminhoArquivoLocal + nomeArquivo);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Servidor: Inputação de arquivo bem sucedida!");

            dataOutputStream.writeUTF(nomeArquivo);

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
        String nomeArquivo = receberNomeArquivo(socket);

        if (verificarArquivoSalvo(nomeArquivo)) {
            File file = new File("src/main/resources/distribuidos/sistemas/projetoprimeiranota/files/" + nomeArquivo);
            file.delete();
            System.out.println("Servidor: Exclusão de arquivo bem sucedida!");
        }
    }

    private String receberNomeArquivo(Socket socket) throws IOException {
        InputStream inputStream;
        try {
            inputStream = socket.getInputStream();
        } catch (NullPointerException error) {
            System.out.println("Servidor: Captura de nome do arquivo mau sucedido!");
            System.err.println(error);
            return null;
        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder nomeArquivo = new StringBuilder();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            nomeArquivo.append(new String(buffer, 0, bytesRead));
        }

        inputStream.close();
        return nomeArquivo.toString();
    }

    private boolean verificarArquivoSalvo(String normeArquivo) {
        if (listarArquivosSalvos() != null) {
            for (File arquivo : listarArquivosSalvos()) if (arquivo.getName().equals(normeArquivo)) return true;
            System.err.println("Servidor: Arquivo não encontrado!");
            return false;
        }
        else {
            System.err.println("Servidor: Diretório inexistente ou vazio!");
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
