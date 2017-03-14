package com.android.dishpatch.dover.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lenovo on 4/21/2016.
 */
public class DoverCentral {
    private static final String TAG = DoverCentral.class.getSimpleName();
    private static DoverCentral sDoverCentral;
    private List<ItemDescription> mDispatchItemDescriptions = new ArrayList<>();
    private HashMap<String,Object> mDishpatchMap = new HashMap<>();
    private List<Item> mItemList = new ArrayList<>();// For Store use
    private List<ItemDescription> mOrderedItemList = new ArrayList<>(); // For customer orderedItem
    private List<Item> mCustomerItemList = new ArrayList<>();//For customer use
    private List<Store> mStoreList = new ArrayList<>();
    private List<Order> mOrderList = new ArrayList<>();
    private List<RunnerTask> mRunnerTaskList = new ArrayList<>();
    private static final String STORE_GET_ITEMS_URL = "http://insvite.com/php/dover/store_get_items.php?store_uuid=";
    private static final String GET_STORE_URL = "http://insvite.com/php/dover/get_stores.php?id_list=";
    private static final String CUSTOMER_GET_ITEM ="http://insvite.com/php/dover/customer_get_items.php?store_uuid=";
    private static final String CUSTOMER_GET_ITEM_BY_CATEGORY="http://insvite.com/php/dover/customer_get_items_by_category.php?";
    private static final String CUSTOMER_GET_ITEM_BY_SEARCH = "http://insvite.com/php/dover/customer_search_items.php?";
    private static final String GET_TRACK_LIST = "http://insvite.com/php/dover/track_order.php?";
    private static final String GET_ORDER_ITEMS = "http://insvite.com/php/dover/get_order_items.php?";
    private static final String GET_RUNNER_TASKS = "http://insvite.com/php/dover/get_runner_task.php?";




    public static DoverCentral get(Context context)
    {
        if(sDoverCentral ==null)
        {
            sDoverCentral =new DoverCentral(context);
        }

        return sDoverCentral;
    }

    private DoverCentral(Context context)
    {


    }


    public List<ItemDescription> getDispatchItemDescriptions()
    {
        return mDispatchItemDescriptions;
    }

    public void setDispatchItemDescriptions(List<ItemDescription> dispatchItemDescriptions) {
        mDispatchItemDescriptions = dispatchItemDescriptions;
    }


    //Manage menu for restaurant
    public List<Item> createMenuList(String uuid)
    {
        OkHttpClient client = new OkHttpClient();
        String request_url = STORE_GET_ITEMS_URL +uuid;
        Request request = new Request.Builder().url(request_url).build();
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful())
            {

                String responseBody = response.body().string();
                Log.v(TAG,responseBody);

                try {
                    mItemList =populateList(responseBody);
                }catch (JSONException e) {
                    Log.v(TAG,e.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return mItemList;

    }

    //For restaurant
    private List<Item> populateList(String responseBody) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray menuArray = jsonObject.getJSONArray("items");

        List<Item> mItemItems = new ArrayList<>();
        for(int i=0; i<menuArray.length(); i++)
        {
            JSONObject item = menuArray.getJSONObject(i);
            int id = item.getInt("item_id");
            String name = item.getString("name");
            double price = item.getDouble("price");
            String category = item.getString("category");
            int weight = item.getInt("quantity");
            String unit = item.getString("unit");
            Boolean availability = item.getBoolean("availability");
            String picture = item.getString("picture");
            Item menu = new Item(id,name,price,availability,category,weight,unit,picture);
            mItemItems.add(menu);



        }

        return mItemItems;

    }


    public List<Store> createStoreList(List<String> keys)
    {
        List<Store> stores = new ArrayList<>();
        int i=0;
        String keyString="";
        for (String k: keys) {
            if(i==0){
                keyString +="'"+k+"'";
            }else{
                keyString+= ",'"+k+"'";
            }
            i++;
        }

        Log.v(TAG,keyString);

        OkHttpClient client = new OkHttpClient();
        String requestString = GET_STORE_URL +keyString;
        Request request = new Request.Builder().url(requestString).build();
        Call call = client.newCall(request);

        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                Log.v(TAG,jsonObject.toString());
                stores = populateStoreList(jsonObject);
                mStoreList = stores;

            }else{
                Log.v(TAG,"Query failed");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }  catch (IOException e){
            e.printStackTrace();
        }
        return stores;
    }

    private List<Store> populateStoreList(JSONObject json) throws JSONException
    {
        JSONArray jsonArray = json.getJSONArray("stores");
        List<Store> stores = new ArrayList<>();
        for(int i=0; i<jsonArray.length(); i++)
        {
            JSONObject restaurantObject = jsonArray.getJSONObject(i);
            int id = Integer.parseInt(restaurantObject.getString("id"));
            String uuid = restaurantObject.getString("uuid");
            String name = restaurantObject.getString("store_name");
            Uri profileUri = Uri.parse(restaurantObject.getString("profile_picture"));
            Log.v(TAG,profileUri.toString());

            Store store = new Store(id,uuid,name,profileUri);
            stores.add(store);
        }

        return stores;
    }

    //Get Item For customers
    public List<Item> createCustomerItemList(String uuid)
    {

        List<Item> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString = CUSTOMER_GET_ITEM +uuid;

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                Log.v(TAG,uuid);

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateCustomerItemList(jsonBody);
                mCustomerItemList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mCustomerItemList;

    }


    private List<Item> populateCustomerItemList(JSONObject jsonObject) throws JSONException{

        List<Item> items = new ArrayList<>();
        JSONArray menuArray = jsonObject.getJSONArray("items");

        for(int i=0; i<menuArray.length(); i++)
        {
            JSONObject menuObject = menuArray.getJSONObject(i);
            int id = Integer.parseInt(menuObject.getString("item_id"));
            String name = menuObject.getString("item_name");
            String category = menuObject.getString("category");
            double price = menuObject.getDouble("price");
            int quantity = Integer.parseInt(menuObject.getString("quantity"));
            String unit = menuObject.getString("unit");
            String picture = menuObject.getString("picture");

            Item item = new Item(id,name,price,category,quantity,unit,picture);
            items.add(item);
        }

        return items;


    }


    public List<Item> createCustomerItemListByCategory(String category,String uuid)
    {
        List<Item> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString = CUSTOMER_GET_ITEM_BY_CATEGORY+"store_uuid="+uuid+"&category="+category;
        Log.v(TAG,requestString);

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                Log.v(TAG,uuid);

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateCustomerItemList(jsonBody);
                mCustomerItemList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mCustomerItemList;
    }

    public List<Item> createCustomerItemListBySearch(String keyword,String uuid)
    {

        List<Item> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString = CUSTOMER_GET_ITEM_BY_SEARCH+"store_uuid="+uuid+"&keyword="+keyword;
        Log.v(TAG,requestString);

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                Log.v(TAG,uuid);

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateCustomerItemList(jsonBody);
                mCustomerItemList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mCustomerItemList;

    }


    //Create track list
    public List<Order> createTrackList(String uuid)
    {
        List<Order> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString = GET_TRACK_LIST+"customer_uuid="+uuid;
        Log.v(TAG,requestString);

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                Log.v(TAG,uuid);

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateTrackList(jsonBody);
                mOrderList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mOrderList;
    }

    private List<Order> populateTrackList(JSONObject jsonBody) throws JSONException {

        List<Order> items = new ArrayList<>();
        JSONArray menuArray = jsonBody.getJSONArray("orders");

        for(int i=0; i<menuArray.length(); i++)
        {
            JSONObject menuObject = menuArray.getJSONObject(i);
            int orderId = Integer.parseInt(menuObject.getString("order_id"));
            int store_id = Integer.parseInt(menuObject.getString("store_id"));
            String store_name= menuObject.getString("store_name");
            String store_uuid = menuObject.getString("store_uuid");
            Uri store_uri  =  Uri.parse(menuObject.getString("profile_picture"));

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(menuObject.getString("date_time"));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            String runner_uuid = menuObject.getString("runner_uuid");

            Order order = new Order(orderId,new Store(store_id,store_uuid,store_name,store_uri),new Runner(runner_uuid),date);

            items.add(order);
        }

        return items;

    }



    public List<ItemDescription> createOrderItemList(int order_id){

        List<ItemDescription> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString = GET_ORDER_ITEMS+"order_id="+order_id;
        Log.v(TAG,requestString);

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();
                Log.v(TAG,order_id+"");

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateOrderItemList(jsonBody);
                mOrderedItemList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mOrderedItemList;
    }

    private List<ItemDescription> populateOrderItemList(JSONObject jsonBody) throws JSONException{

        List<ItemDescription> itemList = new ArrayList<>();

        JSONArray itemArray = jsonBody.getJSONArray("items");

        for(int i=0; i<itemArray.length(); i++)
        {
            JSONObject itemObject = itemArray.getJSONObject(i);
            int order_item_id = Integer.parseInt(itemObject.getString("order_item_id"));
            int id = Integer.parseInt(itemObject.getString("item_id"));
            String name = itemObject.getString("item_name");
            String category = itemObject.getString("category");
            double price = itemObject.getDouble("price");
            int quantity = Integer.parseInt(itemObject.getString("quantity"));
            String unit = itemObject.getString("unit");
            String picture = itemObject.getString("picture_url");
            int order_quantity = Integer.parseInt(itemObject.getString("item_quantity"));
            String remarks = itemObject.getString("remarks");

            Item item = new Item(id,name,price,category,quantity,unit,picture);

            ItemDescription itemDescription = new ItemDescription(order_item_id,item,remarks,order_quantity);
            itemList.add(itemDescription);
        }


        return itemList;

    }



    public void createRunnerTaskList(String runner_uuid){

        List<RunnerTask> temp = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String requestString  = GET_RUNNER_TASKS+"runner_uuid="+runner_uuid;
        Log.v(TAG,requestString);

        Request request = new Request.Builder().url(requestString).build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();

            if(response.isSuccessful())
            {
                String responseBody = response.body().string();

                Log.v(TAG,responseBody);

                JSONObject jsonBody = new JSONObject(responseBody);
                temp = populateOrderTaskList(jsonBody);
                mRunnerTaskList = temp;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<RunnerTask> populateOrderTaskList(JSONObject jsonBody) throws JSONException {

        List<RunnerTask> taskList = new ArrayList<>();
        JSONArray itemArray = jsonBody.getJSONArray("orders");

        for(int i=0; i<itemArray.length(); i++)
        {
            JSONObject itemObject = itemArray.getJSONObject(i);
            JSONObject orderObject = itemObject.getJSONObject("order");
            int orderId = Integer.parseInt(orderObject.getString("order_id"));
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(orderObject.getString("date_time"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            float totalPrice = Float.parseFloat(orderObject.getString("total_price"));

            JSONObject storeObject = itemObject.getJSONObject("store");
            int store_id = Integer.parseInt(storeObject.getString("store_id"));
            String store_uuid = storeObject.getString("store_uuid");
            String store_name = storeObject.getString("store_name");
            String store_profile_pic = storeObject.getString("store_profile_picture");
            Store store = new Store(store_id,store_uuid,store_name,Uri.parse(store_profile_pic));

            Order order = new Order(orderId,store,date,totalPrice);


            JSONObject customerObject = itemObject.getJSONObject("customer");
            String customer_uuid = customerObject.getString("customer_uuid");
            String customer_name = customerObject.getString("customer_name");
            String uri =customerObject.getString("customer_profile_picture");

            Customer customer = new Customer(customer_uuid,customer_name,uri);

            RunnerTask runnerTask = new RunnerTask(customer,order);
            taskList.add(runnerTask);







        }



        return taskList;
    }


    public List<Item> getItemList() {


        return mItemList;
    }

    public void setItemList(List<Item> itemList) {
        mItemList = itemList;
    }

    public Item getMenu(int id)
    {
        for(Item item : mItemList)
        {
            if(item.getItemId()==id)
            {
                return item;
            }

        }

        return null;

    }




    public List<Item> getCustomerItemList() {
        return mCustomerItemList;
    }

    public void setCustomerItemList(List<Item> customerItemList) {
        mCustomerItemList = customerItemList;
    }

    public List<Store> getStoreList() {
        return mStoreList;
    }

    public void setStoreList(List<Store> storeList) {
        mStoreList = storeList;
    }

    public List<ItemDescription> getOrderedItemList() {
        return mOrderedItemList;
    }

    public void setOrderedItemList(List<ItemDescription> orderedItemList) {
        mOrderedItemList = orderedItemList;
    }

    public List<RunnerTask> getRunnerTaskList() {
        return mRunnerTaskList;
    }

    public void setRunnerTaskList(List<RunnerTask> runnerTaskList) {
        mRunnerTaskList = runnerTaskList;
    }

    public void addMenu(Item item)
    {
        mItemList.add(item);
    }
    public void putValue(String key, Object o)
    {
        mDishpatchMap.put(key,o);
    }

    public Object getValue(String key)
    {
        return mDishpatchMap.get(key);
    }
}
