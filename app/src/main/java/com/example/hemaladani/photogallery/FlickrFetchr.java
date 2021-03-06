package com.example.hemaladani.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemaladani on 5/2/17.
 */

public class FlickrFetchr {
//this is new branch
    private static final String API_KEY="20876718ca497c63915ce5ad9519456a", TAG="FlickrFetchr";
    private static final String FETCH_RECENT_METHOD="flickr.photos.getRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static final Uri ENDPOINT=Uri.parse("https://api.flickr.com/services/rest/").buildUpon()
            .appendQueryParameter("api_key",API_KEY).appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1").appendQueryParameter("extras","url_s").build();

    private void parseItems(List<GalleryItem> items,JSONObject jsonBody)throws IOException,JSONException{
            JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
            JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");
        Log.i("Size of elem JSON",photoJsonArray.length()+"");
        for(int i=0;i<photoJsonArray.length();i++){

            JSONObject photoJsonObject=photoJsonArray.getJSONObject(i);
            GalleryItem item=new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));
            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s"));
            item.setmOwner(photoJsonObject.getString("owner"));
            items.add(item);


        }



    }
    public List<GalleryItem> fetchRecentPhotos(){

        String url=buildurl(FETCH_RECENT_METHOD,null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){

        String url=buildurl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }



    private List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items=new ArrayList<>();
        try{

            String jsonString=getUrlString(url);


            JSONObject jsonBody=new JSONObject(jsonString);
            parseItems(items,jsonBody);
            Log.i(TAG,"Received Json:"+jsonString);

        }catch (IOException|JSONException ioe){
            Log.e(TAG,"Failed to fetch items",ioe);
        }
        return items;


    }
    private String buildurl(String method,String query){
        Uri.Builder uriBuilder=ENDPOINT.buildUpon().appendQueryParameter("method",method);
        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.build().toString();

    }
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);

        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        try{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        InputStream in=connection.getInputStream();
        if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){

            throw new IOException(connection.getResponseMessage()+":with"+urlSpec);
        }
        int bytesRead=0;
        byte[] buffer=new byte[1024];
        while((bytesRead=in.read(buffer))>0){
            out.write(buffer,0,bytesRead);
        }
        out.close();
        return out.toByteArray();}
        finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpecs)throws IOException{
        return new String(getUrlBytes(urlSpecs));

    }


}
