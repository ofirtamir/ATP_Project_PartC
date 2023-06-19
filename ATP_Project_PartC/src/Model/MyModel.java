package Model;

import Client.Client;
import Client.IClientStrategy;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import IO.SimpleCompressorOutputStream;
import IO.SimpleDecompressorInputStream;
import Server.Server;
import Server.IServerStrategy;
import algorithms.mazeGenerators.AMazeGenerator;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.*;


import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import static View.Log4J.LOG;

public class MyModel extends Observable implements IModel{

    Position player;
    Maze curr_maze;
    Server generator, solver;
    int genport;
    int solveport;


    @Override
    public boolean save(String fileName) {


        try(OutputStream out = new MyCompressorOutputStream(new FileOutputStream(fileName))) {
            // save maze to a file

            out.write(curr_maze.toByteArray());
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            LOG.fatal("MyModel failed to save file.", e);
        }
        return false;
    }
    @Override
    public boolean load(String fileName){
        byte[] buffer = new byte[65536]; //Maximum maze size according to forum.

        try(InputStream in = new MyDecompressorInputStream(new FileInputStream(fileName))) {
            //read maze from file

            int bytesRead;
            bytesRead = in.read(buffer);
            byte[] mazebytes = Arrays.copyOfRange(buffer, 0, bytesRead);
            in.close();
            curr_maze = new Maze(mazebytes);
            player = curr_maze.getStartPosition();
            return true;
        } catch (IOException e) {
            LOG.error("MyModel failed to load file.", e);

        }
        return false;
    }
    @Override
    public void generateRandomMaze(int row, int col) {

        try {
            curr_maze = CommunicateWithServer_MazeGenerating(row, col);
            player = curr_maze.getStartPosition();
        }
        catch (Exception e){
            LOG.error("MyModel failed to generate maze.", e);
        }

    }

    @Override
    public Maze getMaze() {
        return curr_maze;
    }

    @Override
    public Position getPlayer() {
        return player;
    }

    @Override
    public Solution getSolution(int playerx, int playery) {
        Maze copy = curr_maze;
        copy.setStartPosition(new Position(playerx, playery));
        Solution solution = CommunicateWithServer_SolveSearchProblem(copy);
        return solution;
    }

    @Override
    public void updateCharacterLocation(int direction) {
        Position newpos;

        switch (direction) {
            case 1 -> // UP
                    newpos = player.Up();
            case 2 -> // DOWN
                    newpos = player.Down();
            case 3 -> // LEFT
                    newpos = player.Left();
            case 4 -> // RIGHT
                    newpos = player.Right();
            case 5 -> // DOWNLEFT
            {
                newpos = player.Down();
                newpos = newpos.Left();
            }
            case 6 -> // DOWNRIGHT
            {
                newpos = player.Down();
                newpos = newpos.Right();
            }
            case 7 -> // UPLEFT
            {
                newpos = player.Up();
                newpos = newpos.Left();
            }
            case 8 -> // UPRIGHT
            {
                newpos = player.Up();
                newpos = newpos.Right();
            }
            default ->
                throw new UnsupportedOperationException("Unable to move to given direction.");
        }
        if (validTraversal(newpos))
            player = newpos;
        else
            throw new UnsupportedOperationException("Unable to move to given direction.");

    }

    @Override
    public void connectGenerator(int port, int listeningIntervalMS, IServerStrategy strategy) {
        generator = new Server(port, listeningIntervalMS, strategy);
        generator.start();
        genport = port;
        LOG.info(String.format("Maze generating server connected via port %s.", port));
    }
    private Solution CommunicateWithServer_SolveSearchProblem(Maze toSolve) {
        final Solution[] mazeSolution = new Solution[1];
        try {
            Client client = new Client(InetAddress.getLocalHost(), solveport, new IClientStrategy() {
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

    private Maze CommunicateWithServer_MazeGenerating(int row, int col) {
        row = (Math.max(row, 2));
        col = (Math.max(col, 2));
        final Maze[] maze = new Maze[1];
        try {
            int finalRow = row;
            int finalCol = col;
            Client client = new Client(InetAddress.getLocalHost(), genport, new IClientStrategy() {
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
    @Override
    public void connectSolver(int port, int listeningIntervalMS, IServerStrategy strategy) {
        solver = new Server(port, listeningIntervalMS, strategy);
        solver.start();
        solveport = port;
        LOG.info(String.format("Maze solving server connected via port %s.", port));

    }

    private boolean validTraversal(Position pos){
        if (pos.getRowIndex() < 0 || pos.getColumnIndex() < 0 || pos.getRowIndex() >= curr_maze.getRows() || pos.getColumnIndex() >= curr_maze.getColumns())
            return false;
        return curr_maze.getVal(pos) == 0;
    }

    @Override
    public void stopServers(){
        generator.stop();
        solver.stop();
    }
}
