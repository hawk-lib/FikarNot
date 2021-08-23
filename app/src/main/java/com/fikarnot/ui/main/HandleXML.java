package com.fikarnot.ui.main;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HandleXML {
    private List<String> title = new ArrayList<>();
    private String link = "link";
    private List<String> newslink = new ArrayList<>();
    private List<String> description = new ArrayList<>();
    private String urlString;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;
    public boolean done = false;

    public HandleXML(String url) {
        this.urlString = url;
    }

    public List<NewsModel> getNews() {
        List<NewsModel> newsList = new ArrayList<>();
        if (title.size()==description.size() && title.size()==newslink.size()) {
            for (int i = 1; i < title.size(); i++) {
                NewsModel news = new NewsModel(title.get(i), description.get(i), newslink.get(i));
                newsList.add(news);
            }
            return newsList;
        }else {
            return new ArrayList<NewsModel>();
        }
    }

    public String getLink() {
        return link;
    }


    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text = null;


        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if (name.equals("title")) {
                            title.add(text);
                        }

                        else if(name.equals("link")){
                            newslink.add(text);
                        }

                        else if (name.equals("description")) {
                            description.add(text);
                        } else {
                        }

                        break;
                }

                event = myParser.next();
            }

            parsingComplete = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean fetchXML() {

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            InputStream stream = conn.getInputStream();

            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myparser = xmlFactoryObject.newPullParser();

            myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myparser.setInput(stream, null);

            parseXMLAndStoreIt(myparser);
            stream.close();
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
