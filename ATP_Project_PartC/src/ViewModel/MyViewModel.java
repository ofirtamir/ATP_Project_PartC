package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.util.Pair;

import java.util.ArrayList;

public class MyViewModel {

    private final IModel model;

    public MyViewModel(IModel model) {
        this.model = model;
    }

    public void generateMaze(int rows, int cols) {
        model.generateRandomMaze(rows, cols);
    }


    public Maze getMaze() {
        return model.getMaze();
    }

    public boolean movePlayer(String direction) {
        try {
            switch (direction) {
                case "UP" -> model.updateCharacterLocation(1);
                case "DOWN" -> model.updateCharacterLocation(2);
                case "LEFT" -> model.updateCharacterLocation(3);
                case "RIGHT" -> model.updateCharacterLocation(4);
                case "DOWNLEFT" -> model.updateCharacterLocation(5);
                case "DOWNRIGHT" -> model.updateCharacterLocation(6);
                case "UPLEFT" -> model.updateCharacterLocation(7);
                case "UPRIGHT" -> model.updateCharacterLocation(8);
            }
        } catch (UnsupportedOperationException e) { return false;}
        return true;
    }

    public boolean save(String fileName){
        if (fileName == null)
            return false;
        if (fileName.equals(""))
            return false;
        return model.save(fileName);
    }
    public boolean load(String fileName){
        return model.load(fileName);
    }

    public Pair<Integer, Integer> getPlayerLocation() {
        Position pos = model.getPlayer();

        return new Pair<>(pos.getRowIndex(), pos.getColumnIndex());
    }

    public Pair<Integer, Integer>[] getSolution(Pair<Integer, Integer> player) {
        Solution sol = model.getSolution(player.getKey(), player.getValue());
        ArrayList<AState> solutionPath = sol.getSolutionPath();
        Pair<Integer, Integer>[] toreturn = new Pair[solutionPath.size()];
        for (int i = 0; i < solutionPath.size(); i++) {
            Position pos = solutionPath.get(i).getState();
            toreturn[i] = new Pair<>(pos.getRowIndex(), pos.getColumnIndex());
        }
        return toreturn;
    }

    public void stopServers(){model.stopServers();}
}
