package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.JsonIterator;
import com.hiroshi.cimoc.core.parser.MangaCategory;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends MangaParser {

    public Dmzj() {
        category = new Category();
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://s.acg.178.com/comicsum/search.php?s=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        String jsonString = StringUtils.match("g_search_data = (.*);", html, 1);
        try {
            return new JsonIterator(new JSONArray(jsonString)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        if (object.optInt("hidden", 1) != 1) {
                            String cid = object.getString("id");
                            String title = object.getString("name");
                            String cover = object.getString("cover");
                            long time = object.getLong("last_updatetime") * 1000;
                            String update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                            String author = object.optString("authors");
                            // boolean status = object.getInt("status_tag_id") == 2310;
                            return new Comic(SourceManager.SOURCE_DMZJ, cid, title, cover, update, author);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.textWithSubstring("p.txtDesc", 3);
        String title = body.attr("#Cover > img", "title");
        String cover = body.src("#Cover > img");
        String author = body.text("div.Introduct_Sub > div.sub_r > p:eq(0) > a");
        String update = body.textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
        boolean status = isFinish(body.text("div.Introduct_Sub > div.sub_r > p:eq(2) > a:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        String jsonString = StringUtils.match("initIntroData\\((.*?)\\);", html, 1);
        List<Chapter> list = new LinkedList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    JSONArray data = array.getJSONObject(i).getJSONArray("data");
                    for (int j = 0; j != data.length(); ++j) {
                        JSONObject object = data.getJSONObject(j);
                        String title = object.getString("chapter_name");
                        String path = object.getString("id");
                        list.add(new Chapter(title, path));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.dmzj.com/view/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String jsonString = StringUtils.match("\"page_url\":(\\[.*?\\]),", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, array.getString(i), false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    if (object.optInt("hidden", 1) != 1) {
                        String cid = object.getString("id");
                        String title = object.getString("name");
                        String cover = "http://images.dmzj.com/".concat(object.getString("cover"));
                        Long time = object.has("last_updatetime") ? object.getLong("last_updatetime") * 1000 : null;
                        String update = time == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                        String author = object.optString("authors");
                        list.add(new Comic(SourceManager.SOURCE_DMZJ, cid, title, cover, update, author));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format("http://m.dmzj.com/classify/%s-%s-%s-%s-%s-%%d.json",
                    args[0], args[2], args[4], args[1], args[5]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("冒险", "1"));
            list.add(Pair.create("欢乐向", "2"));
            list.add(Pair.create("格斗", "3"));
            list.add(Pair.create("科幻", "4"));
            list.add(Pair.create("爱情", "5"));
            list.add(Pair.create("竞技", "6"));
            list.add(Pair.create("魔法", "7"));
            list.add(Pair.create("校园", "8"));
            list.add(Pair.create("悬疑", "9"));
            list.add(Pair.create("恐怖", "10"));
            list.add(Pair.create("生活亲情", "11"));
            list.add(Pair.create("百合", "12"));
            list.add(Pair.create("伪娘", "13"));
            list.add(Pair.create("耽美", "14"));
            list.add(Pair.create("后宫", "15"));
            list.add(Pair.create("萌系", "16"));
            list.add(Pair.create("治愈", "17"));
            list.add(Pair.create("武侠", "18"));
            list.add(Pair.create("职场", "19"));
            list.add(Pair.create("奇幻", "20"));
            list.add(Pair.create("节操", "21"));
            list.add(Pair.create("轻小说", "22"));
            list.add(Pair.create("搞笑", "23"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("日本", "1"));
            list.add(Pair.create("内地", "2"));
            list.add(Pair.create("欧美", "3"));
            list.add(Pair.create("港台", "4"));
            list.add(Pair.create("韩国", "5"));
            list.add(Pair.create("其他", "6"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("少年", "1"));
            list.add(Pair.create("少女", "2"));
            list.add(Pair.create("青年", "3"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "0"));
            list.add(Pair.create("连载", "1"));
            list.add(Pair.create("完结", "2"));
            return list;
        }

        @Override
        public boolean hasOrder() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "1"));
            list.add(Pair.create("人气", "0"));
            return list;
        }

    }

}
