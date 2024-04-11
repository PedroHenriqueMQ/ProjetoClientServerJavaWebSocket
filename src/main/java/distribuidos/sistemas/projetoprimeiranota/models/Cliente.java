package distribuidos.sistemas.projetoprimeiranota.models;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Cliente {
    private final String hostAdress;
    private final int port;
    private Socket socket = null;

    public Cliente(String hostAdress, int port) {
        this.hostAdress = hostAdress;
        this.port = port;
    }

    public void realizarConexao() throws IOException {
        try {
            socket = new Socket(this.hostAdress, this.port);
            System.out.println("Conexão bem sucedida!");
        } catch (UnknownHostException error) {
            System.out.println("Conexão mal sucedida!");
            System.err.println(error);
        }
    }

    public void encerrarConexao() throws IOException {
        try {
            socket.close();
            System.out.println("Encerramento de conexão bem sucedido!");
        } catch (NullPointerException error) {
            System.out.println("Encerramento de conexão mal sucedido!");
            System.err.println(error);
        }
    }

    public void enviar(String caminhoArquivoEntrada) throws IOException {
        FileInputStream fileInputStream;
        DataOutputStream dataOutputStream;

        try {
            fileInputStream = new FileInputStream(caminhoArquivoEntrada);
            dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
            System.out.println("Inputação de arquivo bem sucedida!");
        } catch (FileNotFoundException | NullPointerException error) {
            System.out.println("Inputação de arquivo mal sucedida!");
            System.err.println(error);
            return;
        }

        dataOutputStream.writeUTF(new File(caminhoArquivoEntrada).getName());

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytesRead);
        }

        dataOutputStream.flush();
        dataOutputStream.close();
        fileInputStream.close();
    }

    public void requerirDownload(String nomeArquivo) throws IOException {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(nomeArquivo);

            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (NullPointerException error) {
            System.out.println("Conexão com servidor não encontrada");
            System.err.println(error);
        }
    }

    public void receberDownload(String caminhoArquivoSaida) throws IOException {
        try {
            System.out.println("Tô por aqui");
            DataInputStream dataInputStream = new DataInputStream(this.socket.getInputStream());
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

    public void excluir(String nomeArquivo) throws IOException {
        OutputStream outputStream;

        try {
            outputStream = socket.getOutputStream();
        } catch (NullPointerException error) {
            System.out.println("Exclusão de arquivo mau sucedida!");
            System.err.println(error);
            return;
        }

        byte[] bytes = nomeArquivo.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
