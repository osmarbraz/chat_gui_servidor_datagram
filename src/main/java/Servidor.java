
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 *
 * @author osmar
 */
public class Servidor implements Runnable {

    private String nome;
    private int portaServidor;
    private JTextArea txtMensagens;
    private InetAddress[] enderecos;
    
    private int conexoes;
    private int maximoConexoes;
    private boolean replicarMensagens;
    private JLabel lblConexoes;
    
    public Servidor(String nome, int portaServidor, int maximoConexoes, boolean replicarMensagens, JTextArea txtMensagens, JLabel lblConexoes) {
        this.nome = nome;
        this.portaServidor = portaServidor;
        this.maximoConexoes = maximoConexoes;
        this.replicarMensagens = replicarMensagens;
        this.txtMensagens = txtMensagens;
        this.enderecos = new InetAddress[50];
        this.conexoes = 0;
        this.lblConexoes = lblConexoes;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public JTextArea getTxtMensagens() {
        return txtMensagens;
    }

    public void setTxtMensagens(JTextArea txtMensagens) {
        this.txtMensagens = txtMensagens;
    }

    public int getPortaServidor() {
        return portaServidor;
    }

    public void setPortaServidor(int portaServidor) {
        this.portaServidor = portaServidor;
    }

   public int getConexoes() {
        return conexoes;
    }

    public void setConexoes(int conexoes) {
        this.conexoes = conexoes;
        lblConexoes.setText("" + getConexoes());
    }

    public int getMaximoConexoes() {
        return maximoConexoes;
    }

    public void setMaximoConexoes(int maximoConexoes) {
        this.maximoConexoes = maximoConexoes;
    }

    public boolean isReplicarMensagens() {
        return replicarMensagens;
    }

    public void setReplicarMensagens(boolean replicarMensagens) {
        this.replicarMensagens = replicarMensagens;
    }

    @Override
    public void run() {
        try {
            //Abre um server socket na porta especificada
            DatagramSocket serverDatagram = new DatagramSocket(portaServidor);

            txtMensagens.append("\n>>> Servidor " + nome + " no ar! <<<");
            txtMensagens.append("\nEscutando a porta: " + serverDatagram.getLocalPort());
            txtMensagens.append("\nAguardando mensagens!");

            while (true) {

                //Tamanho dos dados do pacote
                byte dado[] = new byte[200];
                //Pacote do datagrama
                DatagramPacket pacote = new DatagramPacket(dado, dado.length);
                //Recupera o pacote do datagrama
                serverDatagram.receive(pacote);

                InetAddress enderecoCliente = pacote.getAddress();
                int porta = pacote.getPort();

                adicionaCliente(enderecoCliente);

                //Recupera o texto recebido        
                String mensagem = new String(pacote.getData());

                txtMensagens.append("\n" + enderecoCliente + " > " + mensagem);
                
                replicaMensagem(mensagem);
            }
        } catch (IOException ioe) {
            txtMensagens.append("\nProblemas de IO");
        }
    }

    public void adicionaCliente(InetAddress enderecoCliente) {
        //Procura o endereço na lista
        boolean existe = false;
        for (int i = 0; i < conexoes; i++) {
            if (enderecoCliente.getHostAddress().equals(enderecos[i].getHostAddress())) {
                existe = true;
            }
        }
        //Se não existe adiciona e conta
        if (existe == false) {
            enderecos[conexoes] = enderecoCliente;
            setConexoes(getConexoes() + 1);
        }
    }
    
     //Envia a mensagem para todos os clientes
    public void replicaMensagem(String mensagem) {
        //Se configurado para replicar mensagens
        if (isReplicarMensagens()) {
            for (int i = 0; i < conexoes; i++) {
                enviaMensagem(mensagem, enderecos[i]);
            }
        }
    }
    
     public void enviaMensagem(String mensagem, InetAddress endereco) {
        try {        
            DatagramSocket datagramServidor = new DatagramSocket();//cria o objeto
            //Converte a mensagem em um vetor de byte
            byte dado[] = mensagem.getBytes();
            //Configura o pacote
            DatagramPacket pacote = new DatagramPacket(dado, dado.length, endereco, getPortaServidor());
            //envia o pacote
            datagramServidor.send(pacote);
        } catch (IOException io) {
          txtMensagens.append("\nProblemas de IO");
        }
    }
}
