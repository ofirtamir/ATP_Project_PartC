package Model;

import Client.Client;
import Client.IClientStrategy;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.Server;
import Server.IServerStrategy;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.*;


import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Observable;

import static View.Log4J.LOG;

public class MyModel extends Observable implements IModel{

    Position playerPos;
    Maze currMaze;
    Server generatorServer, solverServer;
    int genPort;
    int solvePort;

    /*
     Saves the current maze to a file using compression.
     */
    @Override
    public boolean save(String fileName) {
        try(OutputStream out = new MyCompressorOutputStream(new FileOutputStream(fileName))) {
            // save maze to a file
            out.write(currMaze.toByteArray());
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            LOG.fatal("MyModel failed to save file.", e);
        }
        return false;
    }
    /*
        Loads a maze from a file and decompresses it.
     */
    @Override
    public boolean load(String fileName){
        byte[] buffer = new byte[2401]; //maze size as assignment 2 (max 50X50)

        try(InputStream in = new MyDecompressorInputStream(new FileInputStream(fileName))) {
            //read maze from file
            int bytesRead = in.read(buffer);
            byte[] mazeBytes = Arrays.copyOfRange(buffer, 0, bytesRead);
            in.close();
            currMaze = new Maze(mazeBytes);
            playerPos = currMaze.getStartPosition();
            return true;
        } catch (IOException e) {
            LOG.error("MyModel failed to load file.", e);

        }
        return false;
    }

    /*
        Generates a random maze with the specified dimensions.
     */
    @Override
    public void generateRandomMaze(int row, int col) {

        try {
            currMaze = CommunicateWithServer_MazeGenerating(row, col);
            playerPos = currMaze.getStartPosition();
        }
        catch (Exception e){
            LOG.error("MyModel failed to generate maze.", e);
        }

    }

    /*
        Returns the current maze.
     */
    @Override
    public Maze getMaze() {
        return currMaze;
    }

    /*
       Returns the current player position.
     */
    @Override
    public Position getPlayerPos() {
        return playerPos;
    }

    /*
        Retrieves a solution to the maze from the server, given the player's position.
     */
    @Override
    public Solution getSolution(int playerX, int playerY) {
        Maze maze = currMaze;
        maze.setStartPosition(new Position(playerX, playerY));
        return CommunicateWithServer_SolveSearchProblem(maze);
    }

    /*
        Updates the player's position based on the given direction.
     */
    @Override
    public void updateCharacterLocation(int direction) {
        Position newPos;

        switch (direction) {
            case 1 -> // UP
                    newPos = playerPos.Up();
            case 2 -> // DOWN
                    newPos = playerPos.Down();
            case 3 -> // LEFT
                    newPos = playerPos.Left();
            case 4 -> // RIGHT
                    newPos = playerPos.Right();
            case 5 -> // DOWNLEFT
            {
                newPos = playerPos.Down();
                newPos = newPos.Left();
            }
            case 6 -> // DOWNRIGHT
            {
                newPos = playerPos.Down();
                newPos = newPos.Right();
            }
            case 7 -> // UPLEFT
            {
                newPos = playerPos.Up();
                newPos = newPos.Left();
            }
            case 8 -> // UPRIGHT
            {
                newPos = playerPos.Up();
                newPos = newPos.Right();
            }
            default ->
                throw new UnsupportedOperationException("Unable to move to given direction.");
        }
        if (validTraversal(newPos))
            playerPos = newPos;
        else
            throw new UnsupportedOperationException("Unable to move to given direction.");

    }

    /*
        Connects to the maze generation server using the specified port, listening interval, and strategy.
     */
    @Override
    public void connectGenerator(int port, int listeningIntervalMS, IServerStrategy strategy) {
        generatorServer = new Server(port, listeningIntervalMS, strategy);
        generatorServer.start();
        genPort = port;
        LOG.info(String.format("Maze generating server connected via port %s.", port));
    }

    /*
        Communicates with the maze solving server to retrieve a solution for the given maze.
     */
    private Solution CommunicateWithServer_SolveSearchProblem(Maze toSolve) {
        final Solution[] mazeSolution = new Solution[1];
        try {
            Client client = new Client(InetAddress.getLocalHost(), solvePort, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(toSolve); //send maze to server
                        toServer.flush();
                        mazeSolution[0] = (Solution) fromServer.readObject();
                    } catch (Exception e) {
                        LOG.error(e);

                    }
                }
            });
            client.communicateWithServer();
            return mazeSolution[0];
        } catch (UnknownHostException e) {
            LOG.error(e);
        }
        return null;
    }

    /*
        Communicates with the maze generation server to generate a maze with the specified dimensions.
     */
    private Maze CommunicateWithServer_MazeGenerating(int row, int col) {
        row = (Math.max(row, 2));
        col = (Math.max(col, 2));
        final Maze[] maze = new Maze[1];
        try {
            int finalRow = row;
            int finalCol = col;
            Client client = new Client(InetAddress.getLocalHost(), genPort, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{finalRow, finalCol};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[finalRow * finalCol + 1];
                        //allocating byte[] for the decompressed maze -
                        is.read(decompressedMaze); //Fill decompressedMaze with bytes

                        maze[0] = new Maze(decompressedMaze);

                    } catch (Exception e) {
                        LOG.error(e);
                    }
                }
            });
            client.communicateWithServer();
            return maze[0];
        } catch (UnknownHostException e) {
            LOG.error(e);
        }
        return null;
    }
    /*
      Connects to the maze solving server using the specified port, listening interval, and strategy.
     */
    @Override
    public void connectSolver(int port, int listeningIntervalMS, IServerStrategy strategy) {
        solverServer = new Server(port, listeningIntervalMS, strategy);
        solverServer.start();
        solvePort = port;
        LOG.info(String.format("Maze solving server connected via port %s.", port));

    }

    /*
        checks if a given position is a valid traversal point in the maze.
     */
    private boolean validTraversal(Position pos){
        if (pos.getRowIndex() < 0 || pos.getColumnIndex() < 0 || pos.getRowIndex() >= currMaze.getRows() || pos.getColumnIndex() >= currMaze.getColumns())
            return false;
        return currMaze.getVal(pos) == 0;
    }

    @Override
    public void stopServers(){
        generatorServer.stop();
        solverServer.stop();
    }
}
