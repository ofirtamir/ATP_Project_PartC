package Model;

import Server.IServerStrategy;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;

public interface IModel {

    void generateRandomMaze(int row, int col);
    Maze getMaze();
    void updateCharacterLocation(int direction);

    void connectSolver(int port, int listeningIntervalMS, IServerStrategy strategy);
    void connectGenerator(int port, int listeningIntervalMS, IServerStrategy strategy);
    Position getPlayerPos();

    Solution getSolution(int playerx, int playery);

    boolean save(String fileName);

    boolean load(String fileName);

    void stopServers();
}
