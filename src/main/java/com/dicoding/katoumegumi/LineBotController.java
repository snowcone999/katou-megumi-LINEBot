
package com.dicoding.katoumegumi;

import com.google.gson.*;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.linecorp.bot.model.response.BotApiResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

                if (payload.events[0].message.text.contains("Katou terjemahkan ")) {
                    String textAsli = payload.events[0].message.text.substring(25);
                    String terjemahkan = payload.events[0].message.text.substring(18,24);
                    terjemahkan = terjemahkan.replaceAll("\\s+","");
                    try {
                        textAsli = URLEncoder.encode(textAsli,"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    try {
                        String terjemahan = translate(textAsli,terjemahkan);
                        replyToUser(payload.events[0].replyToken,"Terjemahan dari : \n"+textAsli+"\n\n"+terjemahan+"\n\n\"Powered by Yandex.Translate\"\nhttp://translate.yandex.com/");
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
            url = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&order=relevance&q=" + keyword + "&safeSearch=strict&key=AIzaSyDlrK6kokD3dDhSoWQKCz3oMAaJMCqaQqM");
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

}

