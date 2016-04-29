package clientegamepong;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javaPlay.GameEngine;
import javaPlay.GameStateController;
import javaPlay.Keyboard;
import javaPlay.Sprite;

class Player implements GameStateController {

    //Inicialização de variaveis de controle
    private int posBarra1;
    private int posBarra2;
    private int posBolaY;
    private int posBolaX;
    private int pontuacaoA = 0;
    private int pontuacaoB = 0;

    int porta = 5550;
    String serverip = "10.3.4.26";
    int serverport = 5554;
    int playerID = 2;
    ServerSocket socketReceive = null;
    private int largura;
    private int altura;

    private Sprite figuraBola;
    private Sprite barra1;
    private Sprite barra2;
    private Sprite figuraBackground;

    Background background = new Background();
    Bola bola1 = new Bola();
    Barra barraA = new Barra();
    Barra barraB = new Barra();

    public Player() throws IOException {
        porta = porta + playerID;
        altura = GameEngine.getInstance().getGameCanvas().getHeight();
        largura = GameEngine.getInstance().getGameCanvas().getWidth();

        try {
            socketReceive = new ServerSocket(porta);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Inicia a bola no meio da tela
        posBarra1 = altura / 2;
        posBarra2 = altura / 2;
        posBolaY = altura / 2;
        posBolaX = largura / 2;

        try {
            //Carrega as sprites
            figuraBackground = new Sprite("background.png", 1, 790, 600);
            figuraBola = new Sprite("bola.png", 3, 77, 77);
            barra1 = new Sprite("Pong_pad01.png", 3, 25, 100);
            barra2 = new Sprite("Pong_pad02.png", 3, 25, 100);
        } catch (Exception erro) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, erro);
        }
        background.setSprite(figuraBackground);
        bola1.setSprite(figuraBola);
        barraA.setSprite(barra1);
        barraB.setSprite(barra2);
    }

    @Override
    public void step(long l) {

        Thread receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(3);
                } catch (InterruptedException erro) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, erro);
                }
                while (true) {
                    Socket sock = null;
                    try {
                        sock = socketReceive.accept();
                    } catch (IOException ex) {
                        Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try (ObjectInputStream ois = new ObjectInputStream(sock.getInputStream())) {
                        Map<String, Object> requisicao = (Map<String, Object>) ois.readObject();
                        String acao = (String) requisicao.get("acao");
                        switch (acao) {
                            case "moverBola":
                                posBolaX = Integer.parseInt((String) requisicao.get("bolaX"));
                                posBolaY = Integer.parseInt((String) requisicao.get("bolaY"));
                                String pontA = (String) requisicao.get("pontuacaoA");
                                String pontB = (String) requisicao.get("pontuacaoB");
                                if (pontA.equals("")) {
                                    pontuacaoA = 0;
                                } else {
                                    pontuacaoA = (Integer.parseInt(pontA));
                                }
                                if (pontB.equals("")) {
                                    pontuacaoB = 0;
                                } else {
                                    pontuacaoB = (Integer.parseInt(pontB));
                                }
                                break;

                            case "moverBarra":
                                int player = Integer.parseInt((String) requisicao.get("player"));
                                int posicaoY = Integer.parseInt((String) requisicao.get("posicaoY"));
                                if (player == 1) {
                                    posBarra1 = posicaoY;
                                } else if (player == 2) {
                                    posBarra2 = posicaoY;
                                }
                                break;

                            default:
                                System.out.println("Ação: " + acao + " invalida");
                                break;
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        receiveThread.start();

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(3);
                } catch (InterruptedException erro) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, erro);
                }

                //Configração dos controles
                boolean enviaRequisicao = false;
                Keyboard teclado = GameEngine.getInstance().getKeyboard();
                if (playerID == 2) {
                    if ((teclado.keyDown(Keyboard.UP_KEY) == true) && (posBarra2 > 10)) {
                        posBarra2 -= 3;
                        enviaRequisicao = true;
                    }
                    if ((teclado.keyDown(Keyboard.DOWN_KEY) == true) && (posBarra2 < (altura - 150))) {
                        posBarra2 += 3;
                        enviaRequisicao = true;
                    }
                } else if (playerID == 1) {
                    if ((teclado.keyDown(Keyboard.UP_KEY) == true) && (posBarra1 > 10)) {
                        posBarra1 -= 3;
                        enviaRequisicao = true;
                    }
                    if ((teclado.keyDown(Keyboard.DOWN_KEY) == true) && (posBarra1 < (altura - 150))) {
                        posBarra1 += 3;
                        enviaRequisicao = true;
                    }
                }
                if (enviaRequisicao) {
                    Map<String, Object> request = new LinkedHashMap<>();
                    request.put("player", String.valueOf(playerID));
                    request.put("action", "moverBarra");
                    request.put("barraX", String.valueOf(largura - 55));
                    request.put("barraY", String.valueOf(posBarra2));
                    request.put("porta", String.valueOf(porta));
                    sendARequest(request, serverip, serverport);
                }
            }
        });
        sendThread.start();
    }

    //Começa um socket 
    private void sendARequest(Map<String, Object> request, String IP, int PORT) {
        try (Socket s = new Socket(IP, PORT)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
                oos.writeObject(request);
            } catch (UnknownHostException ex) {
                System.out.println("HOST INVALIDO. " + ex);
            } catch (IOException ex) {
                System.out.println("I/0 error. " + ex);
            }
            s.close();
        } catch (IOException ex) {
            System.out.println("ERRO: " + ex);
        }
    }

    /**
     * Metodo Drown. Executado a cada ciclo de clock para redesenhar a tela do
     * Game
     *
     * @param graphic
     */
    @Override
    public void draw(Graphics graphic) {

        background.x = -1;
        background.y = 0;
        background.draw(graphic);

        graphic.setColor(Color.green);
        Font font = new Font("arial", Font.BOLD, 18);
        graphic.setFont(font);
        graphic.drawString("Player A", largura / 2 - 90, 55);
        graphic.drawString("Player B", largura / 2 + 10, 55);
        graphic.drawString(String.valueOf(pontuacaoB), largura / 2 - 60, 75);
        graphic.drawString(String.valueOf(pontuacaoA), largura / 2 + 40, 75);

        bola1.x = posBolaX;
        bola1.y = posBolaY;
        bola1.draw(graphic);

        barraA.x = 13;
        barraA.y = posBarra1;
        barraA.draw(graphic);

        barraB.x = largura - 55;
        barraB.y = posBarra2;
        barraB.draw(graphic);
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
