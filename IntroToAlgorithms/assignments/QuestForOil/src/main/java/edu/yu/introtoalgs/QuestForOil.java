package edu.yu.introtoalgs;

import java.util.*;

public class QuestForOil extends QuestForOilBase {


    char[][] mapa;
    int filas;
    int columnas;
    /**
     * Constructor supplies the map.
     *
     * @param map a non-null, N by M (not necessarily a square!), two-dimensional
     *            matrix in which each element is either an 'S' (safe) or a 'U' (unsafe) to
     *            walk on. It's the client's responsibility to ensure that the matrix isn't
     *            "jagged". The client relinquishes ownership to the implementation.
     */
    public QuestForOil(char[][] map) {
        super(map);
        
        if (map == null) throw new IllegalArgumentException();
        filas = map.length;
        columnas = map[0].length;
        mapa = map;
    }

    @Override
    public int nContiguous(int row, int column) {
        if (row > filas - 1 || row < 0 || column > columnas -1 || column < 0) throw new IllegalArgumentException();
        boolean[][] visited = new boolean[mapa.length][mapa[0].length];
        return bfs(mapa, row, column, visited);
    }



    /*
    private int dfs(char[][] map, int row, int column, boolean[][] visited) {
        if (row < 0 || row >= map.length || column < 0 || column >= map[0].length || map[row][column] == 'U' || visited[row][column]) {
            return 0;
        }

        visited[row][column] = true;
        return 1
                + dfs(map, row + 1, column, visited)
                + dfs(map, row - 1, column, visited)
                + dfs(map, row, column + 1, visited)
                + dfs(map, row, column - 1, visited)
                + dfs(map, row - 1, column - 1, visited)
                + dfs(map, row - 1, column + 1, visited)
                + dfs(map, row + 1, column - 1, visited)
                + dfs(map, row + 1, column + 1, visited);
    }

     */

    private int bfs(char[][] map, int row, int column, boolean[][] visited) {

        if (row < 0 || row >= map.length || column < 0 || column >= map[0].length || map[row][column] == 'U' || visited[row][column])
            return 0;

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, column});
        int i = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.remove();
            int fila = current[0];
            int col = current[1];

            if (fila < 0 || fila >= map.length || col < 0 || col >= map[0].length || map[fila][col] == 'U' || visited[fila][col]) continue;

            visited[fila][col] = true;
            i++;

            meterEnQueue(queue, map, visited, fila + 1, col);
            meterEnQueue(queue, map, visited, fila - 1, col);
            meterEnQueue(queue, map, visited, fila, col + 1);
            meterEnQueue(queue, map, visited, fila, col - 1);
            meterEnQueue(queue, map, visited, fila - 1, col - 1);
            meterEnQueue(queue, map, visited, fila - 1, col + 1);
            meterEnQueue(queue, map, visited, fila + 1, col - 1);
            meterEnQueue(queue, map, visited, fila + 1, col + 1);
        }

        return i;
    }

    private void meterEnQueue(Queue<int[]> queue, char[][] map, boolean[][] visited, int row, int col) {
        if (row >= 0 && row < map.length && col >= 0 && col < map[0].length &&
                map[row][col] == 'S' && !visited[row][col]) {
            queue.add(new int[]{row, col});
        }
    }

}
