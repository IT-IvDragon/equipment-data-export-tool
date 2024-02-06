package com.example.e7tools.service;

import com.example.e7tools.adapter.StatTypeAdapter;
import com.example.e7tools.constant.UITextConstant;
import com.example.e7tools.converter.Converter;
import com.example.e7tools.model.Stat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import okhttp3.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MainService {
    private Gson gson;

    private StringBuilder jsonItems;
    private Map<String, Object> resultMap;

    public MainService() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Stat.class, new StatTypeAdapter())
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .serializeNulls()
                .create();
    }

    /**
     * 请求items
     *
     * @param finalBuffer
     * @return
     */
    public void requestItems(List<String> finalBuffer) {
        //创建请求客户端
        OkHttpClient client = new OkHttpClient();
        //填充请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", finalBuffer);
        String jsonRequestBody = new Gson().toJson(requestBody);
        MediaType mediaType = MediaType.parse("application/json");
        //创建请求体
        RequestBody body = RequestBody.create(mediaType, jsonRequestBody);
        //开始请求
        Request request = new Request.Builder()
                .url("https://krivpfvxi0.execute-api.us-west-2.amazonaws.com/dev/getItems")
                .post(body)
                .addHeader("authority", "krivpfvxi0.execute-api.us-west-2.amazonaws.com")
                .addHeader("cache-control", "max-age=0")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) FribbelsE7Optimizer/1.9.2 Chrome/96.0.4664.55 Electron/16.0.2 Safari/537.36")
                .addHeader("content-type", "application/json")
                .addHeader("accept", "*/*")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("accept-language", "zh-CN")
                .build();
        jsonItems = new StringBuilder();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                jsonItems.append(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 转换items，返回等待导出的map/json字符串
     *
     * @return
     */
    public void converterItems() {
        Map<String, Object> itemsMap = gson.fromJson(jsonItems.toString(), Map.class);
        List<Map<String, Object>> items = Converter.convertItems((List<Map<String, Object>>) itemsMap.get("data"), "heroes");
        List<Map<String, Object>> heroes = Converter.convertUnits((List<List<Map<String, Object>>>) itemsMap.get("units"), "heroes");
        resultMap = new HashMap<>();
        resultMap.put("items", items);
        resultMap.put("heroes", heroes);
    }

    /**
     * 获取json格式的导出文本
     */
    public String getJsonFromResultMap() {
        return gson.toJson(resultMap);
    }

    /**
     * 获取最新版本
     */
    public Map<String, String> getVersion() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(UITextConstant.VERSION_URL)
                .get()
                .build();
        StringBuilder req = new StringBuilder();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                req.append(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> versionMap = new HashMap<>();

        try {
            // 创建 DocumentBuilder 对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // 将字符串转换为 Document 对象
            Document document = builder.parse(new InputSource(new StringReader(req.toString())));


            NodeList fileList = document.getElementsByTagName("file");

            for (int i = 0; i < fileList.getLength(); i++) {
                Node fileNode = fileList.item(i);
                if (fileNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element fileElement = (Element) fileNode;
                    String name = fileElement.getElementsByTagName("name").item(0).getTextContent();
                    String version = fileElement.getElementsByTagName("version").item(0).getTextContent();
                    versionMap.put("version", version);
                    versionMap.put("name", name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionMap;
    }

    /**
     * 获取本地版本
     */
    public Map<String, String> getCurrentVersion() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties");
        Properties props = new Properties();
        props.load(inputStream);
        String version = props.getProperty("version");
        String name = props.getProperty("name");
        Map<String, String> versionMap = new HashMap<>();
        versionMap.put("version", version);
        versionMap.put("name", name);
        return versionMap;
    }
}
