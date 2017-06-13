
package com.dicoding.menirukanmu;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

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

                if (payload.events[0].message.text.contains("Katou cari gambar ")) {
                    String textGambar= payload.events[0].message.text.substring(18);
                    try {
                       msgText = Search(textGambar);
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

    private String UserProfileName(String TargetId) {
        Response<UserProfileResponse> response =
                null;
        try {
            response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .getProfile(TargetId)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String nameUser;
        nameUser = null;
        if (response.isSuccessful()) {
            UserProfileResponse profile = response.body();
            nameUser = profile.getDisplayName();
        } else {
            System.out.println(response.code() + " " + response.message());
        }


        String name = nameUser;
        return name;
    }

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

    private String Search(String text)  throws MalformedURLException, URISyntaxException, IOException {
        String key = "AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM";
        String qry = text;
        String cx  = "016498147224075515320:ukepxzq_vus";
        URL url = new URL(
                "https://www.googleapis.com/customsearch/v1?key="+key+ "&cx=016498147224075515320:ukepxzq_vus&q="+ qry + "&alt=json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        String output;
        String gagal = "gagal";
        System.out.println("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            if(output.contains("\"link\": \"")){
                String link=output.substring(output.indexOf("\"link\": \"")+("\"link\": \"").length(), output.indexOf("\","));
               return link;    //Will print the google search links
            }
        }
        conn.disconnect();
        return gagal;


    }

}
