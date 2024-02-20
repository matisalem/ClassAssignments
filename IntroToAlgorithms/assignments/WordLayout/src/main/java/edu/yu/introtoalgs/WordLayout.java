package edu.yu.introtoalgs;

import java.util.*;

public class WordLayout extends WordLayoutBase{

    boolean[][] ocupados;
    Grid tabla;
    List<String> pa;
    private Map<String, List<LocationBase>> wordLocations = new HashMap<>();
    int columnas;
    int filas;
    int totalPalabras;

    public WordLayout(int nRows, int nColumns, List<String> words) {
        super(nRows, nColumns, words);

        if (nRows < 1 || nColumns < 1 || words == null || words.isEmpty())
            throw new IllegalArgumentException();

        columnas = nColumns;
        filas = nRows;
        totalPalabras = words.size();

        tabla = new Grid(nRows, nColumns);
        ocupados = new boolean[nRows][nColumns];

        List<String> sortedWords = new ArrayList<>(words);
        pa = sortedWords;
        sortedWords.sort(Comparator.comparingInt(String::length).reversed());
        HashMap<Integer, ArrayList<String>> mapa = checkIfFits(nRows, nColumns, sortedWords);

        int conteo = 0;
        // ifDoesntWork();

        if (nColumns >= nRows) {
            for (var i = 0; i < sortedWords.size(); i++) {
                if (i >= nRows) break;
                placeWordInRow(tabla, sortedWords.get(i), i, 0); // Pone palabra mas larga
        //        System.out.println(tabla);
                int lengthDePalabra = sortedWords.get(i).length();
                mapa.get(lengthDePalabra).remove(0); // Saca palabra del lista
                if (nColumns > lengthDePalabra) {  // Si sobra espacio en la fila
                    int resto = nColumns - lengthDePalabra; // cuanto espacio sobra
                    if (mapa.containsKey(resto) && !(mapa.get(resto).isEmpty())) { // si hay una palabra con length restante en el mapa
                        String palabra = mapa.get(resto).remove(0); // borra la palabra del mapoa
                        placeWordInRow(tabla, palabra, i, lengthDePalabra); // la pone
              //          System.out.println(tabla);
                        sortedWords.remove(palabra); // ls saca de la lista
                        if (sortedWords.size() <= i) break; // termina si ya no quedan palabras
                    }

                }
                conteo++;
            }
            if (conteo < sortedWords.size()) { // Si despues de todas las filas quedan palabras para poner
                for (var i = 0; i < sortedWords.size(); i++) {
                    if (i >= nColumns) break; // // Si ya no quedan columnas para poner palabras BREAK
                    int[] pos = proximaPosicion(sortedWords.get(conteo), false); // Se fija en que columna se puede poner
                    if(pos != null) { // si se puede poner
                        placeWordInColumn(tabla, sortedWords.get(conteo), pos[1], pos[0]); // pone la palabra en esa posicion
                        conteo++;
                    }
                    if (conteo == sortedWords.size()) break; // si terminaron las palabras TERMINA
                }
            }

            String palabraHastaAhora = "";

            while (conteo != sortedWords.size()) { // si todabia quedan palabras para poner
                palabraHastaAhora = sortedWords.get(conteo); // IMPORTANTE
                if (sortedWords.get(conteo).isEmpty() || sortedWords.get(conteo).isBlank()) break; // SI las palabras que quedan son vacias
                for (var i = 0; i < sortedWords.size(); i++) {
                    int[] pos = proximaPosicion(sortedWords.get(conteo), true); // Busca proxima posicion en ROW
                    if (pos != null) { // SI hay
                        placeWordInRow(tabla, sortedWords.get(conteo), pos[0], pos[1]); // La pone
                        conteo++;
                        if (conteo == sortedWords.size()) break;
                    }
                }
                if (conteo == sortedWords.size()) break;
                for (var i = 0; i < sortedWords.size(); i++) {
                    int[] pos = proximaPosicion(sortedWords.get(conteo), false);
                    if (pos != null) {
                        placeWordInColumn(tabla, sortedWords.get(conteo), pos[1], pos[0]);
                        conteo++;
                        if (conteo == sortedWords.size()) break;
                    }
                }
                if (conteo == sortedWords.size()) break;
                if (palabraHastaAhora.equals(sortedWords.get(conteo))) {
                    ifDoesntWork();
                    break;
                }
            }
        }
        else {
            for (var i = 0; i < sortedWords.size(); i++) {
                if (i >= nColumns) break; // Si ya no quedan columnas para poner, BREAK
                placeWordInColumn(tabla, sortedWords.get(i), i, 0); // Pone palabra en columna
        //        System.out.println(tabla);
                int lengthDePalabra = sortedWords.get(i).length();
                mapa.get(lengthDePalabra).remove(0); // Saca palabra del mapa
                if (nRows > lengthDePalabra) { // Si sobra espacio en la columna
                    int resto = nRows - lengthDePalabra; // cuanto espacio sobra
                    if (mapa.containsKey(resto) && !(mapa.get(resto).isEmpty())) { // Si hay una palabra con letra restante en mapa
                        String palabra = mapa.get(resto).remove(0); // Agarra la palabra con esa length
                        placeWordInColumn(tabla, palabra, i, lengthDePalabra); // Pone la palabra
                //        System.out.println(tabla);
                        sortedWords.remove(palabra); // Saca la palabra de la lista
                        if (sortedWords.size() <= i) break; // Si estan todas las palabras puestas, BREAK
                    }
                }
                conteo++;
            }

            if (conteo < sortedWords.size()) { // Si todavia quedan palabras
                for (var i = 0; i < sortedWords.size(); i++) {
                    if (i >= nRows) break; // Si ya no quedan filas para poner palabras BREAK
                    int[] pos = proximaPosicion(sortedWords.get(conteo), true); // Si hay una posicion para poner la proxima palabra
                    if(pos != null) { // Si hay
                        placeWordInRow(tabla, sortedWords.get(conteo), pos[0], pos[1]); // Pone la palabra
                        conteo++;
                    }
                    if (conteo == sortedWords.size()) break; // Si no quedan palabras, BREAK
                }
            }

            String palabraHastaAhora = "";

            while (conteo != sortedWords.size()) { // SIGUE hasta que no queden palabras o no pueda poner ninguna
                palabraHastaAhora = sortedWords.get(conteo); // IMPORTANTE
                if (sortedWords.get(conteo).isEmpty() || sortedWords.get(conteo).isBlank()) break; // Si queda una palabra vacia BREAK
                for (var i = 0; i < sortedWords.size(); i++) {
                    int[] pos = proximaPosicion(sortedWords.get(conteo), false); // Proxima posicion en columna
                    if (pos != null) { // SI hay
                        placeWordInColumn(tabla, sortedWords.get(conteo), pos[1], pos[0]); // La pone
                        conteo++;
                        if (conteo == sortedWords.size()) break;
                    }
                }
                if (conteo == sortedWords.size()) break;
                for (var i = 0; i < sortedWords.size(); i++) {
                    int[] pos = proximaPosicion(sortedWords.get(conteo), true); // Busca proxima posicion en ROW
                    if (pos != null) { // Si hay
                        placeWordInRow(tabla, sortedWords.get(conteo), pos[0], pos[1]); // La pone
                        conteo++;
                        if (conteo == sortedWords.size()) break;
                    }
                }
                if (conteo == sortedWords.size()) break;
                if (palabraHastaAhora.equals(sortedWords.get(conteo))) {
                    ifDoesntWork();
                    break;
                }
            }
        }


      //  System.out.println(tabla);
     //   System.out.println(printOcupados());
    }

    private void ifDoesntWork(){
        tabla = new Grid(filas, columnas); // Creo tabla NUEVA
        ocupados = new boolean[filas][columnas]; // Creo ocupados NUEVA
        backtrack(0); // Llama a backtrack
    }
    private boolean backtrack(int index) {
        if (index >= pa.size()) { // Si ya se pusieron todas las palabras TRUE
            return true;
        }
        String word = pa.get(index); // agarrar palabra
        for(int y = 0; y < ocupados.length; y++) {
            for(int x = 0; x < ocupados[y].length; x++) {
                if(sePuedePoner(word, y, x, true)) { // Si la palabra se puede poner en row
                    placeWordInRow(tabla, word, y, x); // la ponemos
                    if (backtrack(index + 1)) { // Llamamos recursivamente backtrack
                        return true;
                    }
                    sacarFila(word, y, x); // si no se pudo poner, saca anterior
                }
                if(sePuedePoner(word, y, x, false)) { // si se puede poner en column
                    placeWordInColumn(tabla, word, x, y); // la ponemos
                    if (backtrack(index + 1)) {
                        return true;
                    }
                    sacarColumna(word, x, y); // si no se pudo, se saca
                }
            }
        }
        return false;
    }
    private void sacarFila(String word, int yPosition, int xPosition) {
        for(int i = 0; i < word.length(); i++) {
            ocupados[yPosition][xPosition + i] = false;
            tabla.grid[yPosition][xPosition + i] = 'A';
        }
        wordLocations.remove(word);
    }
    private void sacarColumna(String word, int xPosition, int yPosition) {
        for(int i = 0; i < word.length(); i++) {
            ocupados[yPosition + i][xPosition] = false;
            tabla.grid[yPosition + i][xPosition] = 'A';
        }
        wordLocations.remove(word);
    }
    private int[] proximaPosicion(String palabra, boolean esHorizontal) {
        int yLen = ocupados.length;
        int xLen = ocupados[0].length;

        for(int y = 0; y < yLen; y++) {
            for(int x = 0; x < xLen; x++) {
                if(!ocupados[y][x] && sePuedePoner(palabra, y, x, esHorizontal)) {
                    return new int[] {y, x};
                }
            }
        }
        return null;
    }
    private boolean sePuedePoner(String palabra, int yPosition, int xPosition, boolean esHorizontal){

        if (!esHorizontal) {
            for (int i = 0; i < palabra.length(); i++) {
                if ((yPosition + i) >= ocupados.length)
                    return false;
                if (ocupados[yPosition + i][xPosition])
                    return false;
            }
            return true;
        } else {
            for (int i = 0; i < palabra.length(); i++) {
                if ((xPosition + i) >= ocupados[0].length)
                    return false;
                try {  if (ocupados[yPosition][xPosition + i]) return false;
                } catch (Exception e){
                    return false;
                }

            }
            return true;
        }
    }
    private void placeWordInRow(Grid tabla, String word, int yPosition, int xPosition) {
        List<LocationBase> locations = new ArrayList<>();
        for(int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            ocupados[yPosition][xPosition + i] = true;
            tabla.grid[yPosition][xPosition + i] = letter;
            locations.add(new LocationBase(yPosition, xPosition + i));
        }
        wordLocations.put(word, locations);
    }
    private void placeWordInColumn(Grid tabla, String word, int xPosition, int yPosition) {
        List<LocationBase> locations = new ArrayList<>();
        for(int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            ocupados[yPosition + i][xPosition] = true;
            tabla.grid[yPosition + i][xPosition] = letter;
            locations.add(new LocationBase(yPosition + i, xPosition));
        }
        wordLocations.put(word, locations);
    }
    private HashMap<Integer, ArrayList<String>> checkIfFits(int nRows, int nColumns, List<String> words){
        HashMap<Integer, ArrayList<String>> mapa = new HashMap<>();
        boolean a = true;
        int count = 0;

        for (var i : words){
            if (i == null || i == "") throw new IllegalArgumentException();
            if (!i.matches("[a-zA-Z0-9]+"))throw new IllegalArgumentException();
            int c = i.length();
            if (a){
                if (c > nRows && c > nColumns)
                    throw new IllegalArgumentException();
                a = false;
            }
            count+=c;
            ArrayList<String> t = new ArrayList<>();
            if(mapa.containsKey(c)){
                mapa.get(c).add(i);
            } else {
                t.add(i);
                mapa.put(c, t);
            }
        }
        if (count > (nRows* nColumns)) throw new IllegalArgumentException();

        return mapa;
    }
    private String printOcupados(){
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());

        int columns = (ocupados[0]).length;
        for (int column = 0; column < columns; column++) {
            sb.append(column);
            sb.append(' ');
        }
        sb.append(System.lineSeparator());

        for (boolean[] rowArray : ocupados) {
            for (boolean b : rowArray) {
                if (b) {
                    sb.append('X');
                } else {
                    sb.append('O');
                }
                sb.append(' ');
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Override
    public List<LocationBase> locations(String word) {
        return wordLocations.getOrDefault(word, Collections.emptyList());
    }

    @Override
    public Grid getGrid() {
        return tabla;
    }


}