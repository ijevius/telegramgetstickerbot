import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import okhttp3.OkHttpClient.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

public class Main {
	
	static {
    try {    	
    	System.loadLibrary("webp-imageio");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load.\n" + e);
      System.exit(1);
    }
  }

	private static long admins[];
	private static long adminsChat;

	static final String TOKEN = "";
	
	private static Timer timer;
	private static TimerTask timerTask;
	
	static int i = 1;
	
	static String lastCommand = "Anon";	
	
	public static void main(String[] args) throws Exception{		
		System.out.println(System.getProperty("java.library.path"));
		
		timerTask = new TimerSchedulePeriod();
		timer = new Timer();
		
		timer.schedule(timerTask, 0, 1000*60);  
		
		new HttpClient();		
		
	}
	
	static void writeText(String l) throws Exception {
		BufferedWriter writer = null;
		try
		{
		    writer = new BufferedWriter( new FileWriter("./latestUpdate.txt"));
		    writer.write(l);

		}
		catch (IOException e)
		{
		}
		finally
		{
		    try
		    {
		        if (writer != null)
		        writer.close();
		    }
		    catch ( IOException e)
		    {
		    }
		}
    }
    

    static long getLatestUpdateId() throws Exception {
        String content;
        content = new String(Files.readAllBytes(Paths.get("./latestUpdate.txt")));
        long r = Long.parseLong(content);
        return r;
    }
	
    public static void sendTextMessage(String text, long user_id){
    	
    }
    
	public static String genReport() throws UnsupportedEncodingException{
		
		String ok = null, time, latestMessage = lastCommand;
		
		time = parseDate(System.currentTimeMillis());
		
		String ex = "";
		
		String state = String.format(ex, ok, time, latestMessage);	
		
		return state;
	}
	
	static String parseDate(long time){
		long unixSeconds = time;	
	Date date = new Date(unixSeconds); 
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy"); 
	sdf.setTimeZone(TimeZone.getTimeZone("GMT+3")); 
	String formattedDate = sdf.format(date);
	return formattedDate;
	}	
	
	public static void sendChatAction(String chat_id, String action) throws Exception{
		sendHttpGet("https://api.telegram.org/bot"+Main.TOKEN+"/sendChatAction?chat_id="+chat_id+"&action="+action);
	}
	
	public static String GetUpdates() throws Exception{
		while(true){			
			String response = sendHttpGet("https://api.telegram.org/bot"+Main.TOKEN+"/getUpdates?limit=100&timeout=9&allowed_updates=[\"message\"]&offset="+String.valueOf((getLatestUpdateId()+1)));	
			
			System.out.println(response);
			JSONArray result = new JSONObject(response).getJSONArray("result");
			if(result.length()!=0){			
			String time = null;
			long lastUpdate = result.getJSONObject(0).getLong("update_id");
			if(result.getJSONObject(0).has("message")){
				time = parseDate(result.getJSONObject(0).getJSONObject("message").getLong("date")*1000L);
				if(result.getJSONObject(0).getJSONObject("message").has("text")){
					String from = String.valueOf(result.getJSONObject(0).getJSONObject("message").getJSONObject("from").getLong("id"));
					sendHttpGet("https://api.telegram.org/bot"+Main.TOKEN+"/sendMessage?chat_id="+from+"&text=Sticker, please");
					
				}
				if(result.getJSONObject(0).getJSONObject("message").has("sticker")){
					String id = result.getJSONObject(0).getJSONObject("message").getJSONObject("sticker").getString("file_id");	
					long mid = result.getJSONObject(0).getJSONObject("message").getLong("message_id");
					long start = System.currentTimeMillis();
					//String a = webp2png(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id)));
					//webp2png(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id)));										      
					//File f = new File(webp2png(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id))));
					String from = String.valueOf(result.getJSONObject(0).getJSONObject("message").getJSONObject("from").getLong("id"));
					sendChatAction(from, "upload_photo");
					File f = new File(w2p(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id))));
					//webp2png(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id)));
					//System.out.println(w2p(downloadFile("https://api.telegram.org/file/bot"+TOKEN+"/"+tgetFilePath(id))));
					sendPhoto(f, from, String.valueOf(mid));							
					long end = System.currentTimeMillis();					
					//System.out.printlnString.format("Finished in %.2f seconds", ((float)(end-start)/1000)));
				}
			JSONObject uid = result.getJSONObject(0).getJSONObject("message").getJSONObject("from");
			lastCommand = uid.getString("first_name");
			if(uid.has("last_name")){
				lastCommand = lastCommand + " "+uid.getString("last_name");
			}
			lastCommand = lastCommand + ", id"+uid.getLong("id");
			if(uid.has("username")){
				lastCommand = lastCommand + ", @"+ uid.getString("username");
			}
			lastCommand = lastCommand + " update: " + lastUpdate;
			System.out.println(String.format("%1$s: %2$s отправил боту сообщение", time, lastCommand));	
			//String report = String.format("%1$s: #update %2$d has been processed", parseDate(System.currentTimeMillis()), lastUpdate);
			//sendHttpGet("https://api.telegram.org/bot"+Main.TOKEN+"/sendMessage?chat_id="+from+"&text="+report+"&disable_notification=true");
			writeText(String.valueOf(lastUpdate));
			}						
			
		}}
		
	}
	static String domain = String.format("https://api.telegram.org/bot%1$s/", TOKEN);
	
	static String tgetFilePath(String id) throws Exception{
		String response = sendHttpGet(domain + "getFile?file_id=" + id);		
		String r = null;
		
		JSONObject object = new JSONObject(response);
		r = object.getJSONObject("result").getString("file_path");
		
		//System.out.println("path="+r);
		return r;
	}	
	
	static String downloadFile(String url) throws MalformedURLException, IOException{
		String movedTo = "./tl-bot/dist/StickerBot/files/";
		String fileName = UUID.randomUUID().toString() + ".webp";
		
		File f = new File(movedTo+fileName);		
		if(f.exists()){
			f.delete();
		}
		
		try(InputStream in = new URL(url).openStream()){
		    Files.copy(in, Paths.get(movedTo+fileName));
		}
		return fileName;
	}
	
	
	static String w2p(String from) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("./tl-bot/dist/StickerBot/files/"+from));
		} catch (IOException e) {			
			e.printStackTrace();
		}
		File outputfile = new File("./tl-bot/dist/StickerBot/files/"+from.replace(".webp", ".png"));		
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return outputfile.getAbsolutePath();
	}
	
	public static String sendHttpGet(String url) throws Exception{
		Builder b = new Builder();
		b.connectTimeout(25, TimeUnit.SECONDS);
		b.readTimeout(25, TimeUnit.SECONDS);
		b.writeTimeout(25, TimeUnit.SECONDS);
		OkHttpClient client = new OkHttpClient();
		client = b.build();	
		String result = null;
		
		Request request = new Request.Builder()
			      .url(url)			      
			      .build();

			  Response response = null;
			  ResponseBody body = null;
			  int code = 0;
			try {
				response = client.newCall(request).execute();	
				body = response.body();
				code = response.code();		
				if(code == 200){
					result =  response.body().string();
				}else{
				    result = null;
				}
				if(code!=200){
					System.out.println("ERROR:\n " + response);
				}				
			} catch (IOException e) {				
				e.printStackTrace();				
			}
			finally{
				body.close();
				
			}
			
			return result;
						 
	}
	
	
	public static void sendPhoto(File file, String user_id, String replyTo) throws IOException{		
		
		//System.out.println("Отправляю " + file.getAbsolutePath() + " " + user_id);
		
						
		RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)				
		        .addFormDataPart("chat_id", user_id)		  
		        .addFormDataPart("disable_notification", "true")
		        .addFormDataPart("reply_to_message_id", replyTo)
		        .addFormDataPart("photo", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file))
		                                                .build();	
		
		Request request = new Request.Builder()
		        .url("https://api.telegram.org/bot"+Main.TOKEN+"/sendPhoto")
		        .addHeader("Content-Length", String.valueOf(file.length())) 
		        .addHeader("Content-Type", "multipart/form-data")	
		        .post(requestBody)		        
		        .build();		
		
		
		  Builder b = new Builder();
			b.connectTimeout(25, TimeUnit.SECONDS);
			b.readTimeout(25, TimeUnit.SECONDS);
			b.writeTimeout(25, TimeUnit.SECONDS);
			b.retryOnConnectionFailure(true);
			OkHttpClient client = new OkHttpClient();			
			client = b.build();	
			
			client.newCall(request).enqueue(new Callback() {
			      @Override public void onFailure(Call call, IOException e) {
			        e.printStackTrace();
			      }

			      @Override public void onResponse(Call call, Response response) throws IOException {
			        if (!response.isSuccessful()) {throw new IOException("Unexpected code " + response);}
			        if(response.code()==200){
			        	file.delete();
			        	new File(file.getAbsolutePath().replace(".png", ".webp")).delete();
			        }
			        response.body().close();
			      }
			    });			
		
		return;
	}

	public static String sendHttpPost(String url) throws Exception {
	
		String result = null;
		
		RequestBody formBody = new FormBody.Builder()
		        .add("chat_id", "")
		        .add("parse_mode", "Markdown")		        
		        .add("text", Main.genReport())		        
		        .build();
		    Request request = new Request.Builder()
		        .url(url)
		        .post(formBody)		       
		        .addHeader("charset", "utf-8")
		        .build();

		    Response response = null;
		    ResponseBody body = null;
		    int code = 0;
		    
		    Builder b = new Builder();
			b.connectTimeout(25, TimeUnit.SECONDS);
			b.readTimeout(25, TimeUnit.SECONDS);
			b.writeTimeout(25, TimeUnit.SECONDS);
			OkHttpClient client = new OkHttpClient();
			client = b.build();	
		    
		    try {
				response = client.newCall(request).execute();	
				body = response.body();
				code = response.code();				
				if(code!=200){
					System.out.println("ERROR:\n " + response);
				}
				if(code == 200){
					result = response.body().string();
				}else{
				    result = null;
				}
			} catch (IOException e) {				
				e.printStackTrace();
			}finally{
				body.close(); 
				client = null;
			}
		    
		    return result;
	}	
	
}


class TimerSchedulePeriod extends TimerTask {	
	@Override
	public void run() {
		System.out.println("OK! " + Main.parseDate(System.currentTimeMillis()));
		try {
			Main.sendHttpPost("https://api.telegram.org/bot"+Main.TOKEN+"/sendMessage");
		} catch (Exception e) {			
			e.printStackTrace();
		}
		try {
			Main.genReport();
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
		}
	}
}

class HttpClient implements Runnable {
	Thread thread;
   
	HttpClient() {
		thread = new Thread(this, "HTTP");		
		thread.start(); 
	}
	
	public void run() {		
		try {			
			Main.GetUpdates();
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}
