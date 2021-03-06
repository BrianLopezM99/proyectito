package com.company;

import javax.xml.crypto.Data;
import java.net.*;
import java.io.*;

public class Server implements Runnable {
    private ServerSocket server;
    private Socket player1;
    private Socket player2;
    private DataInputStream in1;
    private DataInputStream in2;
    private DataOutputStream out1;
    private DataOutputStream out2;
    private String player1Sign;
    private String player2Sign;
    private Board board;
    int port;

    public Server(int port, ServerSocket socket) {
        board = new Board();
        server = socket;
        this.port = port;
        connectToPlayers();
    }
    public void run() {
        System.out.println(Thread.currentThread().getName());
        try {
            //connectToPlayers();
            open();
            boolean done = false;
            while (!done) {
                try {
                    out2.writeUTF("Esperando a jugador 1");
                    playerTurn(in1,out1);
                    done = isGameOver();

                    out1.writeUTF("Esperando a jugador 2");
                    playerTurn(in2,out2);
                    done = isGameOver();
                } catch (IOException ioe) {
                    done = true;
                }
            }
            close();
        } catch (IOException ie) {
            System.out.println("Acceptance Error: " + ie);
        }
    }
    private synchronized void connectToPlayers(){
        try {
            //System.out.println("Binding to port " + port + ", please wait  ...");
            //server = new ServerSocket(port);
            //System.out.println("Server started: " + server);
            System.out.println("Esperando al cliente");
            player1 = server.accept();
            player1Sign = Board.PLAYERS[0];
            System.out.println("Cliente aceptado " + player1);

            System.out.println("Esperando al cliente");
            player2 = server.accept();
            player2Sign = Board.PLAYERS[1];
            System.out.println("Cliente aceptado " + player2);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void open() throws IOException {
        in1 = new DataInputStream(new BufferedInputStream(player1.getInputStream()));
        in2 = new DataInputStream(new BufferedInputStream(player2.getInputStream()));

        out1 = new DataOutputStream(player1.getOutputStream());
        out2 = new DataOutputStream(player2.getOutputStream());
    }
    private void close() throws IOException {
        if (player1 != null) player1.close();
        if (in1 != null) in1.close();
        if (out1 != null) out1.close();

        if (player2 != null) player2.close();
        if (in2 != null) in2.close();
        if (out2 != null) out2.close();
    }
    private int[] readPosition(DataInputStream in, DataOutputStream out) throws IOException {
        int[] position = new int[2];
        boolean inputIsCorrect = false;
        while (!inputIsCorrect) {
            out.writeUTF("Escribe en la fila (del 1 al 3)");
            position[0] = Integer.parseInt(in.readUTF()) - 1;
            out.writeUTF("Escribe en la columna (del 1 al 3)");
            position[1] = Integer.parseInt(in.readUTF()) - 1;
            inputIsCorrect = validatePosition(position);
            if (!inputIsCorrect)
                out.writeUTF("Esta posicion es invalida");
        }
        return position;
    }
    private boolean validatePosition(int[] pos){
        if (pos[0] > 2 || pos[1] > 2)
            return false;
        if (!board.fieldIsEmpty(pos))
            return false;
        return true;
    }
    private void insertSign(int[] pos){
        board.putSign(pos);
    }
    private void sendBoardToPlayers() throws IOException{
        out1.writeUTF(board.showBoard());
        out2.writeUTF(board.showBoard());
    }
    private void playerTurn(DataInputStream in, DataOutputStream out) throws IOException{
        insertSign(readPosition(in,out));
        sendBoardToPlayers();
    }
    private boolean isGameOver() throws IOException{
        boolean gameIsOver = false;
        if (gameIsOver = (board.checkWinner() != null)){
            announceWinner();
            out1.writeUTF("kusbais");
            out2.writeUTF("kusbais");
        }
        return gameIsOver;
    }
    private void announceWinner() throws IOException{
        String winner = board.checkWinner();
        if (winner.equals(player1Sign)){
            out1.writeUTF("Ganaste wuwu");
            out2.writeUTF("Perdiste, F");
        } else {
            out1.writeUTF("Perdiste, F");
            out2.writeUTF("Ganaste wuwu");
        }
    }

}
