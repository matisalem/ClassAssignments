package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.print.Doc;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File file;
 //   private transient long DoFcume;
     public DocumentPersistenceManager(File baseDir){
         if (baseDir != null) {
             this.file = baseDir;
         } else {
             this.file = new File(System.getProperty("user.dir"));
         }

         try {
             file.createNewFile();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

     }

    private class DocumentSerializer implements JsonSerializer<Document> {

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jason = new JsonObject();

            jason.addProperty("URI", src.getKey().toString());
            jason.add("map", context.serialize(src.getWordMap()));
            if (src.getDocumentTxt() != null) {
                jason.addProperty("text", src.getDocumentTxt());
            } else {
                byte[] bytes = src.getDocumentBinaryData();
                /*
                for (byte b : bytes) {
                    System.out.print(b + " ");
                }
                System.out.println();

                 */
                String base64Encoded = jakarta.xml.bind.DatatypeConverter.printBase64Binary(src.getDocumentBinaryData());
                jason.addProperty("bytes", base64Encoded);
            }
            return jason;
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new DocumentSerializer()).create();
        JsonObject jason = new JsonObject();
        String json = gson.toJson(val);
       // System.out.println(json);

        File archivo = new File(locationInDisk(uri));
        archivo.getParentFile().mkdirs();
        archivo.createNewFile();

        Writer fileWriter = new FileWriter(archivo);
        fileWriter.write(json);
        fileWriter.flush();
        fileWriter.close();
    }

    private String locationInDisk(URI uri){
        String s = uri.toString();
        if(uri.getScheme() != null)
            s = s.replace(uri.getScheme(), "");

        if (s.startsWith("://"))
                s = s.replace("://", "");

        s = s.replaceAll("[<>:\"|?*@ ]", "");

        if (s.endsWith("/"))
            s = s.substring(0, s.length() - 1);

        s = s + ".json";
        s = s.replace("/", File.separator).replace("\\", File.separator);
        return file + File.separator + s;
    }

    private class DocumentDeserializer implements JsonDeserializer<Document> {

        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Gson gson = new Gson();
            Document documento = null;
            JsonObject json = jsonElement.getAsJsonObject();
            String uriString = json.get("URI").getAsString();
            URI uri;
            try {
                uri = new URI(uriString);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            if (json.has("bytes")){
                String crip = json.get("bytes").getAsString();
                byte[] decodedBytes = Base64.getDecoder().decode(crip);
                //  System.out.println("decoded: " + decodedBytes);
                byte[] dd = decodedBytes;
                documento = new DocumentImpl(uri, dd);
            } else {
                String texto = json.get("text").getAsString();
                String mapaFalse = json.get("map").toString();
                Type t = new TypeToken<HashMap<String, Integer>>(){}.getType();
                HashMap<String, Integer> mapa = gson.fromJson(mapaFalse, t);
                documento = new DocumentImpl(uri, texto, mapa);
            }
         //   System.out.println("BOCA JUNIORS CARAJO");
            return documento;
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        File archivo = new File(locationInDisk(uri));
        if (!archivo.exists())
            return null;
        GsonBuilder gson = new GsonBuilder();

        byte[] bytes = Files.readAllBytes(archivo.toPath());
        String contenido = new String(bytes);

        gson.registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer()).create();
        Document documento = gson.setLenient().create().fromJson(contenido, DocumentImpl.class);
        delete(uri);
        return documento;
    }


    @Override
    public boolean delete(URI uri) throws IOException {
        File eliminado = new File(locationInDisk(uri));
        boolean seElimino = eliminado.delete();
        return seElimino;
    }
}
