
package com.dicoding.katoumegumi;

import com.google.gson.*;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.linecorp.bot.model.response.BotApiResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import retrofit2.Response;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
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
                replyToUser(payload.events[0].replyToken, "Halo Semuanya silahkan ketik 'Katou keyword' untuk melihat keyword :D");
            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Halo Semuanya silahkan ketik 'Katou keyword' untuk melihat keyword :D");
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
                if (payload.events[0].message.text.equals("Katou")) {
                    replyToUser(payload.events[0].replyToken,msgText = "Iya");
                }

                if (payload.events[0].message.text.equals("Katou ramal")) {
                    replyToUser(payload.events[0].replyToken,getRandom(ramal));
                }

                if (payload.events[0].message.text.contains("Katou cari video ")) {
                    String keyword = payload.events[0].message.text.substring(17);
                    List videoItem = null;
                    try {
                        videoItem = ambilUrlVideoId(keyword);
                    } catch (Exception e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Gagal menemukan video atau LIMIT");
                    }
                    Random rand = new Random();
                    String urlVideoId = String.valueOf(videoItem.get(rand.nextInt(videoItem.size())));
                    String urlYoutubeDownload = ambilUrlVideo(urlVideoId.replace("[","").replace("]",""));
                    String urlYoutubeThumbnail = "https://i.ytimg.com/vi/"+urlVideoId.replace("[","").replace("]","")+"/default.jpg";
//                    String urlYoutubeDownload = ambilUrlVideo();
//                    String urlYoutubeThumbnail = "https://i.ytimg.com/vi/hjTAakwP924/hqdefault.jpg?sqp=-oaymwEXCPYBEIoBSFryq4qpAwkIARUAAIhCGAE=&rs=AOn4CLD4jQPxagi91z8A6Oy6H_mlpElfBw";
                    replyToUserVideo(payload.events[0].replyToken,urlYoutubeDownload,urlYoutubeThumbnail);
                }

                if (payload.events[0].message.text.contains("Katou download musik ")) {
                    String keyword = payload.events[0].message.text.substring(21);
                    List videoItem = ambilUrlVideoIdMp3(keyword);
                    String urlVideoId = String.valueOf(videoItem.get(0));
                    String urlVideoTitle = String.valueOf(videoItem.get(1));
                    String title = urlVideoTitle;
                    String linkDownload = "Link : http://mp3you.tube/get/?direct=https://www.youtube.com/watch?v="+urlVideoId.replace("[","").replace("]","");
                    String messageLink = title+"\n\n"+linkDownload;
                    replyToUser(payload.events[0].replyToken,messageLink);
                }

                if (payload.events[0].message.text.contains("Katou stalk ")) {
                    String keyword = payload.events[0].message.text.substring(12);
                    List listIg = null;
                    try {
                        listIg = SearchIg(keyword);
                        String urlPost = null;
                        String urlImg = null;
                        String comments = null;
                        String likes = null;
                        String deskripsi_post = null;
                        String is_count = String.valueOf(listIg.get(0));
                        String is_private = String.valueOf(listIg.get(1));
                        String username = String.valueOf(listIg.get(2));
                        String followers = String.valueOf(listIg.get(3));
                        String following = String.valueOf(listIg.get(4));
                        String profile_pic = String.valueOf(listIg.get(5));
                        String profile_url = "https://www.instagram.com/"+username;

                        String five = "Followers : "+followers+"\nFollowing : "+following;

                        if(!is_private.equals("true") && !is_count.equals("0") ) {
                            urlImg = String.valueOf(listIg.get(6));
                            urlPost = String.valueOf(listIg.get(7));
                            comments = String.valueOf(listIg.get(8));
                            likes = String.valueOf(listIg.get(9));
                            deskripsi_post = "Likes : "+likes+"\nComments : "+comments;
                            replyToUserTemplateIgCarousel(payload.events[0].replyToken, profile_pic, username, five, deskripsi_post, profile_url, urlImg, urlPost);
                        }else{
                            replyToUserTemplateIgButton(payload.events[0].replyToken,profile_pic,username,five,profile_url);
                        }

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"User instagram dengan id "+keyword+" gagal ditemukan");
                    } catch (NullPointerException e){
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"User instagram dengan id "+keyword+" gagal ditemukan");
                    }
                }

                if (payload.events[0].message.text.contains("Katou apa itu ")) {
                    String textTanya= payload.events[0].message.text.substring(14);
                    textTanya = textTanya.replaceAll("\\s+","_");
                    try {
                        String jawaban = wiki(textTanya);
                        replyToUser(payload.events[0].replyToken,jawaban);
                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (NullPointerException e){
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Tidak ditemukan hasil dengan keyword : "+textTanya);
                    }
                }

                if (payload.events[0].message.text.contains("Katou tulis ")) {
                    String textGambar = payload.events[0].message.text.substring(12);
                    try {
                        textGambar = URLEncoder.encode(textGambar,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    replyToUserImage(payload.events[0].replyToken,"https://chart.apis.google.com/chart?chs=300x50&cht=p3&chtt="+textGambar+"&chts=FFFFFF,24&chf=bg,s,000000","https://chart.apis.google.com/chart?chs=300x50&cht=p3&chtt="+textGambar+"&chts=FFFFFF,24&chf=bg,s,000000");
                }

                if (payload.events[0].message.text.contains("Katou ubah alay ")) {
                    String textKeyword = payload.events[0].message.text.substring(16);
                    replyToUser(payload.events[0].replyToken,ubahAlay(textKeyword));
                }

                if (payload.events[0].message.text.contains("Katou terjemahkan alay ")) {
                    String textKeyword = payload.events[0].message.text.substring(23);
                    replyToUser(payload.events[0].replyToken,translateAlay(textKeyword));
                }

                if (payload.events[0].message.text.contains("Katou cari gambar ")) {
                    String textGambar= payload.events[0].message.text.substring(18);
                    textGambar = textGambar.replaceAll("\\s+","+");
                    try {
                       List url = searchImg(textGambar);
                       Random rand = new Random();
                       String linkImg = String.valueOf(url.get(rand.nextInt(url.size())));
                       String httpnyaLink = linkImg.substring(0,5);
                       replyToUserImage(payload.events[0].replyToken,
                               httpnyaLink.equals("http:")?linkImg.replace("http","https"):linkImg, httpnyaLink.equals("http:")?linkImg.replace("http","https"):linkImg);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Gambar gagal ditemukan atau LIMIT");
                    }
//                    replyToUser(payload.events[0].replyToken,"sedang perbaikan");
                }

                if (payload.events[0].message.text.contains("Katou cari lokasi ")) {
                    String textTitle = payload.events[0].message.text.substring(18);
                    String textLokasi= payload.events[0].message.text.substring(18);
                    textLokasi = textLokasi.replaceAll("\\s+","+");
                    try {
                        List results = searchLocation(textLokasi);
                        String address = String.valueOf(results.get(0));
                        double lat = Double.valueOf(String.valueOf(results.get(1)));
                        double lng = Double.valueOf(String.valueOf(results.get(2)));
                        replyToUserLocation(payload.events[0].replyToken,textTitle,address,lat,lng);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Lokasi gagal ditemukan");
                    } catch (Exception e){
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Lokasi gagal ditemukan");
                    }

                }

                if (payload.events[0].message.text.contains("Katou cuaca ")) {
                    String namaKota = payload.events[0].message.text.substring(12);
                    try {
                        replyToUser(payload.events[0].replyToken,forecastWeather(namaKota));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (payload.events[0].message.text.contains("Katou stiker nyanca hai")) {
                    replyToUserImage(payload.events[0].replyToken,"https://image.ibb.co/b32P4k/Nyanca_chibi_hai.png","https://image.ibb.co/b32P4k/Nyanca_chibi_hai.png");
                }

                if (payload.events[0].message.text.contains("Katou 9gag ")) {
                    String textKeyword = payload.events[0].message.text.substring(11);
                    try {
                        List<String> urlImg = search9gag(textKeyword);
                        replyToUser9gag(payload.events[0].replyToken, urlImg.get(0),urlImg.get(1));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Gagal menemukan section "+textKeyword+" di 9GAG");
                    } catch (IOException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Gagal menemukan section "+textKeyword+" di 9GAG");
                    }
                }else if (payload.events[0].message.text.equals("Katou 9gag")) {
                    try {
                        List<String> urlImg = search9gag("hot");
                        replyToUser9gag(payload.events[0].replyToken, urlImg.get(0),urlImg.get(1));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                if (payload.events[0].message.text.contains("Katou terjemahkan ")) {
                    String textAsli = payload.events[0].message.text.substring(24);
                    String textAsli1 = payload.events[0].message.text.substring(24);
                    String terjemahkan = payload.events[0].message.text.substring(18,24);
                    terjemahkan = terjemahkan.replaceAll("\\s+","");
                    try {
                        textAsli = URLEncoder.encode(textAsli,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    try {
                        String terjemahan = translate(textAsli,terjemahkan);
                        replyToUser(payload.events[0].replyToken,"Terjemahan dari : \n"+textAsli1+"\n\n"+terjemahan+"\n\n\"Powered by Yandex.Translate\"\nhttp://translate.yandex.com/");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        replyToUser(payload.events[0].replyToken,"Gagal Menterjemahkan");
                    }

                }

                if (payload.events[0].message.text.contains("Katou ucapkan selamat ulang tahun ke ")) {
                    String textUltah = payload.events[0].message.text.substring(0,37);
                    String namaUltah = payload.events[0].message.text.substring(37);
                    replyToUser(payload.events[0].replyToken,"Selamat Ulang Tahun "+namaUltah+" :D");
                }

                if (payload.events[0].message.text.contains("Katou berapa ")) {
                    String textBerapa = payload.events[0].message.text.substring(0,12);
                    String angka = payload.events[0].message.text.substring(12);
                    String angkaNumber = "20";
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    double hitung = 0;
                    try {
                        hitung = (double) engine.eval(angka);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                    String hasil = String.valueOf(hitung);
                    replyToUser(payload.events[0].replyToken,"Hasil dari "+angka+" adalah "+hasil);
                }

//                if (payload.events[0].message.text.equals("Katou siapa namaku ?")) {
//                    String iduser = payload.events[0].source.userId;
//                    msgText = "Namamu " +  UserProfileName(iduser) ;
//                }
            }
        }
         
        return new ResponseEntity<String>(HttpStatus.OK);
    }


    private void replyToUserTemplateIgCarousel(String rToken, String urlImg, String username, String deskripsiProfil, String deskripsiPost,String profileUrl, String urlPostImg, String urlPost){
        URIAction urlProfil = new URIAction("Ke profil",profileUrl);
        URIAction urlPoster = new URIAction("Ke postingan",urlPost);
        URIAction urlDownload = new URIAction("Download gambar post",urlPostImg);
        List<Action> action = new ArrayList<Action>();
        action.add(urlProfil);
        action.add(urlPoster);
        action.add(urlDownload);

        CarouselColumn profil = new CarouselColumn(urlImg,username,deskripsiProfil,action);
        CarouselColumn postingan = new CarouselColumn(urlPostImg,"Postingan Terakhir",deskripsiPost,action);
        List<CarouselColumn> columns = new ArrayList<CarouselColumn>();
        columns.add(profil);
        columns.add(postingan);

        Template carousel = new CarouselTemplate(columns);
        TemplateMessage templateMessage = new TemplateMessage("Stalk",carousel);

        ReplyMessage replyMessage = new ReplyMessage(rToken, templateMessage);
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

    private void replyToUserTemplateIgButton(String rToken, String urlImg, String username, String deskripsiProfil,String profileUrl){
        URIAction urlProfil = new URIAction("Ke profil",profileUrl);
        List<Action> action = new ArrayList<Action>();
        action.add(urlProfil);

        Template carousel = new ButtonsTemplate(urlImg,username,deskripsiProfil,action);
        TemplateMessage templateMessage = new TemplateMessage("Stalk",carousel);

        ReplyMessage replyMessage = new ReplyMessage(rToken, templateMessage);
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

    private void replyToUser9gag(String rToken, String textTitle, String urlImg){
        TextMessage title = new TextMessage(textTitle);
        ImageMessage image = new ImageMessage(urlImg,urlImg);

        List<Message> item = new ArrayList<Message>();
        item.add(title);
        item.add(image);
        ReplyMessage replyMessage = new ReplyMessage(rToken,item);
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

    private void replyToUserLocation(String rToken, String text, String address, double lat, double lng){
        LocationMessage textMessage = new LocationMessage(text,address,lat,lng);
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

    private List<String> searchImg(String text)  throws MalformedURLException, URISyntaxException, IOException {
        String key = "AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM";
        String qry = text;
        String cx = "016498147224075515320:ukepxzq_vus";
        String fileType = "png,jpg";
        String searchType = "image";
        URL url = new URL(
                "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + cx + "&q=" + qry + "&fileType=" + fileType + "&searchType=" + searchType + "&num=10&safe=high&alt=json");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonArray items = jsonElement.getAsJsonObject().getAsJsonArray("items");

        List<String> list = null;
        list = new ArrayList<String>();

        for (JsonElement it : items) {
            JsonObject itemsObj = it.getAsJsonObject();
            String link = itemsObj.get("link").getAsString();
            list.add(link);
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
    private static String forecastWeather(String text)  throws MalformedURLException, URISyntaxException, IOException {

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

    private void replyToUserVideo(String rToken, String videoUrl, String thumbnailURl){
        VideoMessage videoMessage = new VideoMessage(videoUrl,thumbnailURl);
        ReplyMessage replyMessage = new ReplyMessage(rToken, videoMessage);
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

    private static String ambilUrlVideo(String text) {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://keepvid.com/?url=http%3A%2F%2Fyoutube.com%2Fwatch%3Fv%3D"+text).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements buttonUnduh = doc.select("td:contains(480p)").first().parent().children().get(3).getAllElements();
        String linkhref = buttonUnduh.attr("href");
        return linkhref;
    }

    private static List<String> ambilUrlVideoIdMp3(String text) {
        String keyword = text;
        keyword = keyword.replace(" ", "+");

        URL url = null;
        try {
            url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=" + keyword + "&key=AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection request = null;
        try {
            request = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonElement jsonElement = null;
        try {
            jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonArray items = jsonElement.getAsJsonObject().getAsJsonArray("items");

        List<String> list = null;

        for (JsonElement it : items) {
            JsonObject itemsObj = it.getAsJsonObject();
            JsonObject id = itemsObj.get("id").getAsJsonObject();
            JsonObject snippet = itemsObj.get("snippet").getAsJsonObject();
            String videoId = id.get("videoId").getAsString();
            String title = snippet.get("title").getAsString();
            list = new ArrayList<String>();
            list.add(videoId);
            list.add(title);
            return list;
        }

        return list;
    }

    private static List<String> ambilUrlVideoId(String text) {
        String keyword = text;
        keyword = keyword.replace(" ", "+");

        URL url = null;
        try {
            url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=5&order=relevance&q=" + keyword + "&safeSearch=strict&key=AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection request = null;
        try {
            request = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            request.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonElement jsonElement = null;
        try {
            jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonArray items = jsonElement.getAsJsonObject().getAsJsonArray("items");

        List<String> list = null;
        list = new ArrayList<String>();

        for (JsonElement it : items) {
            JsonObject itemsObj = it.getAsJsonObject();
            JsonObject id = itemsObj.get("id").getAsJsonObject();
            String kind = id.get("kind").getAsString();

            if(kind.equals("youtube#video")) {
                String videoId = id.get("videoId").getAsString();
                list.add(videoId);
            }
        }

        return list;
    }

    private static List SearchIg(String text)  throws MalformedURLException, URISyntaxException, IOException, FileNotFoundException {
        URL url = new URL(
                "https://www.instagram.com/"+text+"/?__a=1");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject user = jsonElement.getAsJsonObject().get("user").getAsJsonObject();
        JsonObject media = user.get("media").getAsJsonObject();
        JsonArray nodes = media.get("nodes").getAsJsonArray();

        String count = media.get("count").getAsString();
        String username = user.get("username").getAsString();
        JsonObject followers = user.get("followed_by").getAsJsonObject();
        JsonObject follows = user.get("follows").getAsJsonObject();
        String folowers = followers.get("count").getAsString();
        String following = follows.get("count").getAsString();
        String profile_pic = user.get("profile_pic_url").getAsString();
        String is_private = user.get("is_private").getAsString();

        List<String> list = null;
        list = new ArrayList<String>();
        list.add(count);
        list.add(is_private);
        list.add(username);
        list.add(folowers);
        list.add(following);
        list.add(profile_pic);

        if(is_private != "true") {
            for (JsonElement it : nodes) {
                JsonObject items = it.getAsJsonObject();
                JsonObject comments = items.get("comments").getAsJsonObject();
                JsonObject likes = items.get("likes").getAsJsonObject();
                String src = items.get("thumbnail_src").getAsString();
                String code = "https://www.instagram.com/p/" + items.get("code").getAsString();
                String commentCount = comments.get("count").getAsString();
                String likeCount = likes.get("count").getAsString();
                list.add(src);
                list.add(code);
                list.add(commentCount);
                list.add(likeCount);
            }
        }


        return list;
    }

    private static List<String> searchLocation(String address)  throws  URISyntaxException, IOException, JsonIOException {
        String key = "AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM";
        URL url = new URL(
                "https://maps.googleapis.com/maps/api/geocode/json?address="+address+"&key="+key);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement jsonElement = new JsonParser().parse(new InputStreamReader((InputStream) request.getContent()));
        JsonArray results = jsonElement.getAsJsonObject().getAsJsonArray("results");

        List<String> list = null;
        list = new ArrayList<String>();

        for (JsonElement it : results) {
            JsonObject itemsObj = it.getAsJsonObject();
            JsonObject geometry = itemsObj.get("geometry").getAsJsonObject();
            JsonObject location = geometry.get("location").getAsJsonObject();
            String address_formated = itemsObj.get("formatted_address").getAsString();
            if(address_formated.length() > 100){
                address_formated = address_formated.substring(0,97)+"...";
            }
            String lat = location.get("lat").getAsString();
            String lng = location.get("lng").getAsString();
            list.add(address_formated);
            list.add(lat);
            list.add(lng);
        }

        return list;
    }

    private static String translate(String Text,String lang)  throws  URISyntaxException, IOException, JsonIOException {
        String key = "trnsl.1.1.20170707T101448Z.97d0be7226643896.37de4347e7f5ac433179a779aadeb974e50247b7";
        URL url = new URL(
                "https://translate.yandex.net/api/v1.5/tr/translate?key=" + key + "&text=" + Text + "&lang=" + lang + "&format=plain&option=");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        InputStream xml = request.getInputStream();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        String textHasil = null;
        try {
            org.w3c.dom.Document doc = db.parse(xml);
            org.w3c.dom.Element text =  doc.getDocumentElement();
            textHasil = text.getTextContent();

        } catch (SAXException e) {
            e.printStackTrace();
        }
        return textHasil;
    }

    private static List<String> search9gag(String Text) throws  URISyntaxException, IOException, JsonIOException{
        Document doc;
        List<String> list = null;
        list = new ArrayList<String>();

        doc = Jsoup.connect("https://9gag.com/"+Text).get();

        Elements getImage = doc.select(".badge-item-img");

        List<String> titleText = new ArrayList<String>();
        List<String> img = new ArrayList<String>();

        for(Element titleElement : getImage){
            String title = titleElement.attr("alt");
            titleText.add(title);
            System.out.println(title);
        }

        for(Element imgElement : getImage) {
            String linkImg = imgElement.attr("src");
            img.add(linkImg);
            System.out.println(linkImg);
        }

        Random rnd = new Random();
        int i = rnd.nextInt(img.size());
        String titleReturn = titleText.get(i);
        String imgReturn = img.get(i);

        List<String> item = new ArrayList<String>();
        item.add(titleReturn);
        item.add(imgReturn);

        return  item;
    }

    private static HttpResponse<String> gambarTeks(String Text)  throws  URISyntaxException, IOException, JsonIOException {
        HttpResponse<String> response = null;
        try {
                response = Unirest.get("https://img4me.p.mashape.com/?fcolor=000000&font=trebuchet&size=30&text=Test+Me&type=png")
                    .header("X-Mashape-Key", "eIn259Ue9cmshGou0GrwMhPQa67Wp1qy2eajsn9LrjzqOV2wiy")
                    .header("Accept", "text/plain")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static String ubahAlay(String teks) {
        String teksKeyword = teks.toLowerCase();

        String[] abjadBener = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        String[] abjadAlay = {"4", "6", "c", "D", "3", "F", "9", "H", "!", "j", "k", "1", "m", "11", "0", "p", "Q", "12", "s", "7", "v", "V", "w", "*", "y", "z"};

        String hasilConvert = null;

        hasilConvert = teksKeyword
                .replace(abjadBener[0], abjadAlay[0])
                .replace(abjadBener[1], abjadAlay[1])
                .replace(abjadBener[2], abjadAlay[2])
                .replace(abjadBener[3], abjadAlay[3])
                .replace(abjadBener[4], abjadAlay[4])
                .replace(abjadBener[5], abjadAlay[5])
                .replace(abjadBener[6], abjadAlay[6])
                .replace(abjadBener[7], abjadAlay[7])
                .replace(abjadBener[8], abjadAlay[8])
                .replace(abjadBener[9], abjadAlay[9])
                .replace(abjadBener[10], abjadAlay[10])
                .replace(abjadBener[11], abjadAlay[11])
                .replace(abjadBener[12], abjadAlay[12])
                .replace(abjadBener[13], abjadAlay[13])
                .replace(abjadBener[14], abjadAlay[14])
                .replace(abjadBener[15], abjadAlay[15])
                .replace(abjadBener[16], abjadAlay[16])
                .replace(abjadBener[17], abjadAlay[17])
                .replace(abjadBener[18], abjadAlay[18])
                .replace(abjadBener[19], abjadAlay[19])
                .replace(abjadBener[20], abjadAlay[20])
                .replace(abjadBener[21], abjadAlay[21])
                .replace(abjadBener[22], abjadAlay[22])
                .replace(abjadBener[23], abjadAlay[23])
                .replace(abjadBener[24], abjadAlay[24])
                .replace(abjadBener[25], abjadAlay[25]);


        return hasilConvert;
    }

    private static String translateAlay(String teks) {
        String teksKeyword = teks.toLowerCase();

        String[] abjadAlay = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        String[] abjadBener = {"4", "6", "c", "D", "3", "F", "9", "H", "!", "j", "k", "L", "m", "11", "0", "p", "Q", "12", "s", "7", "v", "V", "w", "*", "y", "z"};

        String hasilConvert = null;

        hasilConvert = teksKeyword
                .replace(abjadBener[0], abjadAlay[0])
                .replace(abjadBener[1], abjadAlay[1])
                .replace(abjadBener[2], abjadAlay[2])
                .replace(abjadBener[3], abjadAlay[3])
                .replace(abjadBener[4], abjadAlay[4])
                .replace(abjadBener[5], abjadAlay[5])
                .replace(abjadBener[6], abjadAlay[6])
                .replace(abjadBener[7], abjadAlay[7])
                .replace(abjadBener[8], abjadAlay[8])
                .replace(abjadBener[9], abjadAlay[9])
                .replace(abjadBener[10], abjadAlay[10])
                .replace(abjadBener[11], abjadAlay[11])
                .replace(abjadBener[12], abjadAlay[12])
                .replace(abjadBener[13], abjadAlay[13])
                .replace(abjadBener[14], abjadAlay[14])
                .replace(abjadBener[15], abjadAlay[15])
                .replace(abjadBener[16], abjadAlay[16])
                .replace(abjadBener[17], abjadAlay[17])
                .replace(abjadBener[18], abjadAlay[18])
                .replace(abjadBener[19], abjadAlay[19])
                .replace(abjadBener[20], abjadAlay[20])
                .replace(abjadBener[21], abjadAlay[21])
                .replace(abjadBener[22], abjadAlay[22])
                .replace(abjadBener[23], abjadAlay[23])
                .replace(abjadBener[24], abjadAlay[24])
                .replace(abjadBener[25], abjadAlay[25]);


        return hasilConvert;
    }

}

