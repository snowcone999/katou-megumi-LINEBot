
package com.dicoding.katoumegumi;

import com.google.gson.*;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{
    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;
    
    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;

    @RequestMapping(value="/callback", method=RequestMethod.POST)
    public ResponseEntity<String> callback(
        @RequestHeader("X-Line-Signature") String aXLineSignature,
        @RequestBody String aPayload)
    {
        final String text=String.format("The Signature is: %s",
            (aXLineSignature!=null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid=new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if(aPayload!=null && aPayload.length() > 0)
        {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);

        String msgText = " ";
        String idTarget = " ";
        String eventType = payload.events[0].type;

        if (eventType.equals("join")){
            if (payload.events[0].source.type.equals("group")){
                replyToUser(payload.events[0].replyToken, "Halo Semuanya :D");
            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Halo Semuanya :D");
            }
        } else if (eventType.equals("message")){
            if (payload.events[0].source.type.equals("group")){
                idTarget = payload.events[0].source.groupId;
            } else if (payload.events[0].source.type.equals("room")){
                idTarget = payload.events[0].source.roomId;
            } else if (payload.events[0].source.type.equals("user")){
                idTarget = payload.events[0].source.userId;
            }

            if (!payload.events[0].message.type.equals("text")){

            } else {
                if (payload.events[0].message.text.equals("Hai Katou")) {
                    msgText = "Hai juga" ;
                }

                if (payload.events[0].message.text.equals("Katou")) {
                    msgText = "Iya";
                }

                if (payload.events[0].message.text.equals("Katou ramal")) {
                    msgText = getRandom(ramal);
                }

//                if (payload.events[0].message.text.equals("Katou cari video")) {
//                    String videoUrl = "https://www.youtube.com/watch?v=us2SQF9nbPI";
//                    String thumbnailUrl = "https://i.ytimg.com/vi/hjTAakwP924/hqdefault.jpg?custom=true&w=246&h=138&stc=true&jpg444=true&jpgq=90&sp=68&sigh=Y9NwGg7Kltm6L-tr1TpUlaCnEOw";
//                    replyToUserVideo(payload.events[0].replyToken, videoUrl, thumbnailUrl);
//                }

                if (payload.events[0].message.text.contains("Katou apa itu ")) {
                    String textTanya= payload.events[0].message.text.substring(14);
                    textTanya = textTanya.replaceAll("\\s+","_");
                    try {
                        String jawaban = wiki(textTanya);
                        msgText = jawaban;
                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (NullPointerException e){
                        e.printStackTrace();
                        msgText = "Tidak ditemukan hasil dengan keyword : "+textTanya;
                    }
                }

//                if (payload.events[0].message.text.contains("Katou tulis ")) {
//                    String textGambar= payload.events[0].message.text.substring(12);
//                    try {
//                        String urlImg = ambilGambar(textGambar);
//                        replyToUserImage(payload.events[0].replyToken,urlImg,urlImg);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

                if (payload.events[0].message.text.contains("Katou cari gambar ")) {
                    String textGambar= payload.events[0].message.text.substring(18);
                    textGambar = textGambar.replaceAll("\\s+","+");
                    try {
                       List url = Search(textGambar);
                       String linkImg = String.valueOf(url.get(0));
                       String httpnyaLink = linkImg.substring(0,5);
                       String thumbnailLinkImg = String.valueOf(url.get(1));
                       replyToUserImage(payload.events[0].replyToken,
                               httpnyaLink.equals("http:")?linkImg.replace("http","https"):linkImg, thumbnailLinkImg);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (payload.events[0].message.text.contains("Katou cuaca ")) {
                    String namaKota = payload.events[0].message.text.substring(12);
                    try {
                        msgText = forecastweather(namaKota);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (payload.events[0].message.text.contains("Oke Katou ucapkan selamat ulang tahun ke ")) {
                    String textUltah = payload.events[0].message.text.substring(0,41);
                    String namaUltah = payload.events[0].message.text.substring(41);
                    msgText = "Selamat Ulang Tahun "+namaUltah+" :D";
                }

                if (payload.events[0].message.text.contains("Katou berapa ")) {
                    String textBerapa = payload.events[0].message.text.substring(0,12);
                    String angka = payload.events[0].message.text.substring(12);
                    String angkaNumber = "20";
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    int hitung = 0;
                    try {
                        hitung = (int) engine.eval(angka);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                    String hasil = String.valueOf(hitung);
                    msgText = "Hasil dari "+angka+" adalah "+hasil;
                }

//                if (payload.events[0].message.text.equals("Katou siapa namaku ?")) {
//                    String iduser = payload.events[0].source.userId;
//                    msgText = "Namamu " +  UserProfileName(iduser) ;
//                }

                if (!msgText.contains("bot leave")){
                    try {
                        getMessageData(msgText, idTarget);
                    } catch (IOException e) {
                        System.out.println("Exception is raised ");
                        e.printStackTrace();
                    }
                } else {
                    if (payload.events[0].source.type.equals("group")){
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }
         
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private void getMessageData(String message, String targetID) throws IOException{
        if (message!=null){
            pushMessage(targetID, message);
        }
    }

    private void replyToUser(String rToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                .create(lChannelAccessToken)
                .build()
                .replyMessage(replyMessage)
                .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void replyToUserImage(String rToken, String imageUrl, String previewURl){
        ImageMessage imgMessage = new ImageMessage(imageUrl,previewURl);
        ReplyMessage replyMessage = new ReplyMessage(rToken, imgMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushMessage(String sourceId, String txt){
        TextMessage textMessage = new TextMessage(txt);
        PushMessage pushMessage = new PushMessage(sourceId,textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
            .create(lChannelAccessToken)
            .build()
            .pushMessage(pushMessage)
            .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void leaveGR(String id, String type){
        try {
            if (type.equals("group")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .leaveGroup(id)
                    .execute();
                System.out.println(response.code() + " " + response.message());
            } else if (type.equals("room")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .leaveRoom(id)
                    .execute();
                System.out.println(response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

//    private String UserProfileName(String TargetId) {
//        Response<UserProfileResponse> response =
//                null;
//        try {
//            response = LineMessagingServiceBuilder
//                    .create(lChannelAccessToken)
//                    .build()
//                    .getProfile(TargetId)
//                    .execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String nameUser;
//        nameUser = null;
//        if (response.isSuccessful()) {
//            UserProfileResponse profile = response.body();
//            nameUser = profile.getDisplayName();
//        } else {
//            System.out.println(response.code() + " " + response.message());
//        }
//
//
//        String name = nameUser;
//        return name;
//    }

    //ramal Quotes
    String[] ramal ={
            "Berhati-hatilah hari ini adalah hari tersial mu",
            "Hari ini mungkin agak menyusahkan bagimu jadi berhati-hatilah",
            "Hari ini mungkin kamu akan menemukan jodohmu",
            "Hari ini mungkin akan sangat menguntungkan bagi keuanganmu",
            "Tiada hari yang lebih baik dari hari ini bagimu"
    };

    private String getRandom(String[] array){
        Random n = new Random();

        int i = n.nextInt(array.length);
        String  textArray = array[i];

        return textArray;
    }

    private List<String> Search(String text)  throws MalformedURLException, URISyntaxException, IOException {
        String key = "AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM";
        String qry = text;
        String cx = "016498147224075515320:ukepxzq_vus";
        String fileType = "png,jpg";
        String searchType = "image";
        URL url = new URL(
                "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + cx + "&q=" + qry + "&fileType=" + fileType + "&searchType=" + searchType + "&num=1&alt=json");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonArray items = jsonElement.getAsJsonObject().getAsJsonArray("items");

        List<String> list = null;

        for (JsonElement it : items) {
            JsonObject itemsObj = it.getAsJsonObject();
            JsonObject imgObj = itemsObj.get("image").getAsJsonObject();
            String link = itemsObj.get("link").getAsString();
            String thumbnailLink = imgObj.get("thumbnailLink").getAsString();
            list = new ArrayList<String>();
            list.add(link);
            list.add(thumbnailLink);
            return list;
        }

       return list;
    }

    private String wiki(String text) throws IOException{
        String urls = "https://id.wikipedia.org/wiki/"+text;
        // Connect to the URL using java's native library
        URL url = new URL("https://id.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="+text);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonElement pages = jsonElement.getAsJsonObject().get("query").getAsJsonObject().get("pages");

        Set<Map.Entry<String, JsonElement>> entrySet = pages.getAsJsonObject().entrySet();

        JsonElement yourDesiredElement = null;

        for(Map.Entry<String,JsonElement> entry : entrySet){
            yourDesiredElement = entry.getValue();
        }

        String extract = yourDesiredElement.getAsJsonObject().get("extract").getAsString();
        if(extract.length() > 1900) {
            extract = extract.substring(0, 1900) + "...";
        }
        String gagalCari = "Link dialihkan ke "+urls;

        if(extract.equals("")){
            return gagalCari;
        }else{
            return extract+" Read More : "+urls;
        }
    }
    private static String forecastweather(String text)  throws MalformedURLException, URISyntaxException, IOException {

        // Connect to the URL using java's native library
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+text+"&units=metric&APPID=2505c1215671faf783b59b44620d4218");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonElement main = jsonElement.getAsJsonObject().get("main");
        JsonElement wind = jsonElement.getAsJsonObject().get("wind");
        String name = jsonElement.getAsJsonObject().get("name").getAsString();


        String suhu = main.getAsJsonObject().get("temp").getAsString()+" c";
        String kelembaban = main.getAsJsonObject().get("humidity").getAsString()+" %";
        String tekanan = main.getAsJsonObject().get("pressure").getAsString()+" HPa";
        String kecepatan_angin = wind.getAsJsonObject().get("speed").getAsString()+" m/s";

        String cuaca ="Temperatur di kota "+name+" : "+suhu+", Kelembaban : "+kelembaban+", Tekanan udara : "+tekanan+", dan Kecepatan angin : "+kecepatan_angin;

        return cuaca;
    }

//    private String lirik(String artis,String lagu) throws IOException{
//        String url = "https://lirik.kapanlagi.com/artis/"+artis+"/"+lagu;
//        Document doc;
//        doc = Jsoup.connect(url).get();
//        Elements lirik = doc.select(" .mw-parser-output p");
//        String jawaban = lirik.text();
//
//        if(lirik != null){
//            return jawaban;
//        }else{
//            return null;
//        }
//    }

//    private void replyToUserVideo(String rToken, String videoUrl, String thumbnailURl){
//        VideoMessage videoMessage = new VideoMessage(videoUrl,thumbnailURl);
//        ReplyMessage replyMessage = new ReplyMessage(rToken, videoMessage);
//        try {
//            Response<BotApiResponse> response = LineMessagingServiceBuilder
//                    .create(lChannelAccessToken)
//                    .build()
//                    .replyMessage(replyMessage)
//                    .execute();
//            System.out.println("Reply Message: " + response.code() + " " + response.message());
//        } catch (IOException e) {
//            System.out.println("Exception is raised ");
//            e.printStackTrace();
//        }
//    }

//    private String ambilGambar(String text) throws Exception{
//        String key = text;
//        BufferedImage bufferedImage = ImageIO.read(new File(getClass().getResource("test.jpg").toExternalForm()));
//        Graphics graphics = bufferedImage.getGraphics();
//        graphics.setColor(Color.BLACK);
//        graphics.setFont(new Font("Arial Black", Font.BOLD, 30));
//        graphics.drawString(key, 200, 200);
//        ImageIO.write(bufferedImage, "jpg", new File(
//                getClass().getResource("image.jpg").toExternalForm()));
//
//        String link = getClass().getResource("image.jpg").toExternalForm();
//
//        return link;
//    }

}
